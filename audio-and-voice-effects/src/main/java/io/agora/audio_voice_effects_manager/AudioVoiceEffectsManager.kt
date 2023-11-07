package io.agora.audio_voice_effects_manager

import android.content.Context
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.AUDIO_EQUALIZATION_BAND_FREQUENCY
import io.agora.rtc2.IAudioEffectManager
import io.agora.rtc2.IRtcEngineEventHandler


class AudioVoiceEffectsManager(context: Context?) : AuthenticationManager(context) {
    private var audioEffectManager: IAudioEffectManager? = null
    private val baseEventHandler: IRtcEngineEventHandler? // To extend the event handler from the base class

    init {
        baseEventHandler = super.iRtcEngineEventHandler
    }

    override fun setupAgoraEngine(): Boolean {
        val result = super.setupAgoraEngine()

        // Set the audio scenario and audio profile
        agoraEngine?.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO);
        agoraEngine?.setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING);
        return result
    }

    fun startMixing(audioFilePath: String, loopBack: Boolean, cycle: Int, startPos: Int) {
        agoraEngine?.startAudioMixing(audioFilePath, loopBack, cycle, startPos)
    }

    fun stopMixing() {
        agoraEngine?.stopAudioMixing()
    }

    fun playEffect(soundEffectId: Int, soundEffectFilePath: String) {
        if (audioEffectManager == null) {
            // Set up the audio effects manager
            audioEffectManager = agoraEngine?.audioEffectManager
            // Pre-load sound effects to improve performance
            audioEffectManager?.preloadEffect(soundEffectId, soundEffectFilePath)
        }

        audioEffectManager!!.playEffect(
            soundEffectId,  // The ID of the sound effect file.
            soundEffectFilePath,  // The path of the sound effect file.
            0, 1.0,  // The pitch of the audio effect. 1 represents the original pitch.
            0.0, 100.0,  // The volume of the audio effect. 100 represents the original volume.
            true,  // Whether to publish the audio effect to remote users.
            0 // The playback starting position of the audio effect file in ms.
        )
    }

    fun pauseEffect(soundEffectId: Int) {
        audioEffectManager!!.pauseEffect(soundEffectId)
    }

    fun resumeEffect(soundEffectId: Int) {
        audioEffectManager!!.resumeEffect(soundEffectId)
    }

    fun applyVoiceBeautifierPreset(beautifier: Int) {
        // Use a preset value from Constants. For example, Constants.CHAT_BEAUTIFIER_MAGNETIC
        agoraEngine?.setVoiceBeautifierPreset(beautifier);
    }

    fun applyAudioEffectPreset(preset: Int) {
        // Use a preset value from Constants. For example, Constants.VOICE_CHANGER_EFFECT_HULK
        agoraEngine?.setAudioEffectPreset(preset)
    }

    fun applyVoiceConversionPreset(preset: Int) {
        // Use a preset value from Constants. For example, Constants.VOICE_CHANGER_CARTOON
        agoraEngine?.setVoiceConversionPreset(preset)
    }

    fun applyLocalVoiceFormant(preset: Double) {
        // The value range is [-1.0, 1.0]. The default value is 0.0,
        agoraEngine?.setLocalVoiceFormant(preset)
    }

    fun setVoiceEqualization(bandFrequency: AUDIO_EQUALIZATION_BAND_FREQUENCY, bandGain: Int) {
        // Set local voice equalization.
        // The first parameter sets the band frequency. The value ranges between 0 and 9.
        // Each value represents the center frequency of the band: 31, 62, 125, 250, 500, 1k, 2k, 4k, 8k, and 16k Hz.
        // The second parameter sets the gain of each band. The value ranges between -15 and 15 dB.
        // The default value is 0.
        agoraEngine?.setLocalVoiceEqualization(bandFrequency, bandGain)
    }

    fun setVoicePitch(value: Double) {
        //  The value range is [0.5,2.0] default value is 1.0
        agoraEngine?.setLocalVoicePitch(value)
    }

    fun setAudioRoute(enableSpeakerPhone: Boolean) {
        // Disable the default audio route
        agoraEngine?.setDefaultAudioRoutetoSpeakerphone(false)
        // Enable or disable the speakerphone temporarily
        agoraEngine?.setEnableSpeakerphone(enableSpeakerPhone)
    }

    override val iRtcEngineEventHandler: IRtcEngineEventHandler
    get() = object : IRtcEngineEventHandler() {

        // Occurs when the audio effect playback finishes.
        override fun onAudioEffectFinished(soundId: Int) {
            super.onAudioEffectFinished(soundId)
            sendMessage("Audio effect finished")
            audioEffectManager!!.stopEffect(soundId)
            // Notify the UI
            val eventArgs: Map<String, Any> = mapOf("soundId" to soundId)
            mListener?.onEngineEvent("onAudioEffectFinished", eventArgs)
        }

        // Listen for the event that the token is about to expire
        override fun onTokenPrivilegeWillExpire(token: String) {
            baseEventHandler!!.onTokenPrivilegeWillExpire(token)
        }

        // Reuse events handlers from the base class
        override fun onUserJoined(uid: Int, elapsed: Int) {
            baseEventHandler!!.onUserJoined(uid, elapsed)
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            baseEventHandler!!.onJoinChannelSuccess(channel, uid, elapsed)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            baseEventHandler!!.onUserOffline(uid, reason)
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            connectionStateChanged(state, reason)
        }

    }
}