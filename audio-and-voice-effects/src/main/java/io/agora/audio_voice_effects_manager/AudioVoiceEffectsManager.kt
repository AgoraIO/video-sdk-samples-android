package io.agora.audio_voice_effects_manager

import android.content.Context
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.IAudioEffectManager
import io.agora.rtc2.IRtcEngineEventHandler


class AudioVoiceEffectsManager(context: Context?) : AuthenticationManager(context) {
    private var audioEffectManager: IAudioEffectManager? = null
    private val baseEventHandler: IRtcEngineEventHandler? // To extend the event handler from the base class

    init {
        baseEventHandler = super.iRtcEngineEventHandler
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

        override val iRtcEngineEventHandler: IRtcEngineEventHandler
        get() = object : IRtcEngineEventHandler() {

            // Occurs when the audio effect playback finishes.
            override fun onAudioEffectFinished(soundId: Int) {
                super.onAudioEffectFinished(soundId)
                sendMessage("Audio effect finished")
                audioEffectManager!!.stopEffect(soundId)
                mListener.
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