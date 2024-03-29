package io.agora.android_reference_app

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import io.agora.audio_voice_effects_manager.AudioVoiceEffectsManager
import io.agora.rtc2.Constants

class AudioVoiceEffectsActivity : BasicImplementationActivity() {
    private lateinit var audioVoiceEffectsManager: AudioVoiceEffectsManager

    private val soundEffectId = 1 // Unique identifier for the sound effect file
    private val soundEffectFilePath =
        "https://www.soundjay.com/human/applause-01.mp3" // URL or path to the sound effect
    private var soundEffectStatus = 0
    private var voiceEffectIndex = 0
    private var audioPlaying = false // Manage the audio mixing state
    private val audioFilePath =
        "https://www.kozco.com/tech/organfinale.mp3" // URL or path to the audio mixing file
    private var playEffectButton: Button? = null
    private var voiceEffectButton:Button? = null
    private var speakerphoneSwitch: SwitchCompat? = null

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_audio_voice_effects


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speakerphoneSwitch = findViewById(R.id.SwitchSpeakerphone)
        speakerphoneSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            audioVoiceEffectsManager.setAudioRoute(isChecked)
        }
    }

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        audioVoiceEffectsManager = AudioVoiceEffectsManager(this)
        agoraManager = audioVoiceEffectsManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        audioVoiceEffectsManager.joinChannelWithToken()
    }

    @Suppress("UNUSED_PARAMETER")
    fun audioMixing(view: View) {
        val button: Button =  findViewById(R.id.AudioMixingButton)
        audioPlaying = !audioPlaying

        if (audioPlaying) {
            button.text = getString(R.string.stop_audio_mixing)
            try {
                audioVoiceEffectsManager. startMixing(audioFilePath, false, 1, 0)
                showMessage("Audio playing")
            } catch (e: Exception) {
                showMessage("Exception playing audio\n$e")
            }
        } else {
            audioVoiceEffectsManager.stopMixing()
            button.setText(R.string.start_audio_mixing)
        }
    }

    fun playSoundEffect(view: View) {
        if (playEffectButton == null) playEffectButton = view as Button

        when (soundEffectStatus) {
            0 -> { // Stopped
                audioVoiceEffectsManager.playEffect(soundEffectId, soundEffectFilePath)
                playEffectButton?.text = getString(R.string.pause_audio_effect)
                soundEffectStatus = 1
            }
            1 -> { // Playing
                audioVoiceEffectsManager.pauseEffect(soundEffectId)
                soundEffectStatus = 2
                playEffectButton?.text = getString(R.string.resume_audio_effect)
            }
            2 -> { // Paused
                audioVoiceEffectsManager.resumeEffect(soundEffectId)
                soundEffectStatus = 1
                playEffectButton?.text  =getString(R.string.pause_audio_effect)
            }
        }
    }

    override fun handleEngineEvent(eventName: String, eventArgs: Map<String, Any>) {
        super.handleEngineEvent(eventName, eventArgs)
        when (eventName) {
            "onAudioEffectFinished" -> {
                // Update the UI
                soundEffectStatus = 0 // Stopped
                runOnUiThread { playEffectButton!!.text = getString(R.string.play_audio_effect) }
            }
        }
    }

    fun applyVoiceEffect(view: View) {
        if (voiceEffectButton == null) voiceEffectButton = view as Button
        voiceEffectIndex++

        if (voiceEffectIndex == 1) {
            audioVoiceEffectsManager.applyVoiceBeautifierPreset(Constants.CHAT_BEAUTIFIER_MAGNETIC)
            voiceEffectButton!!.text = getString(R.string.voice_effect_chat_beautifier)
        } else if (voiceEffectIndex == 2) {
            audioVoiceEffectsManager.applyVoiceBeautifierPreset(Constants.SINGING_BEAUTIFIER)
            voiceEffectButton!!.text = getString(R.string.voice_effect_singing_beautifier)
        } else if (voiceEffectIndex == 3) {
            // Turn off previous effect
            audioVoiceEffectsManager.applyVoiceBeautifierPreset(Constants.VOICE_BEAUTIFIER_OFF)
            // Modify the timbre using the formantRatio
            // Range is [-1.0, 1.0], [giant, child] default value is 0.
            audioVoiceEffectsManager.applyLocalVoiceFormant(0.6)
            voiceEffectButton!!.text = getString(R.string.voice_effect_adjust_formant)
        } else if (voiceEffectIndex == 4) {
            // Remove previous effect
            audioVoiceEffectsManager.applyLocalVoiceFormant(0.0)
            // Apply audio effect preset
            audioVoiceEffectsManager.applyAudioEffectPreset(Constants.VOICE_CHANGER_EFFECT_HULK)
            voiceEffectButton!!.text = getString(R.string.audio_effect_hulk)
        } else if (voiceEffectIndex == 5) {
            // Remove previous effect
            audioVoiceEffectsManager.applyAudioEffectPreset(Constants.AUDIO_EFFECT_OFF)
            // Apply voice conversion preset
            audioVoiceEffectsManager.applyVoiceConversionPreset(Constants.VOICE_CHANGER_CARTOON)
            voiceEffectButton!!.text = getString(R.string.audio_effect_voice_changer)
        } else if (voiceEffectIndex == 6) {
            // Remove previous effect
            audioVoiceEffectsManager.applyVoiceConversionPreset(Constants.VOICE_CONVERSION_OFF)
            // Set voice equalization
            audioVoiceEffectsManager.setVoiceEqualization(
                Constants.AUDIO_EQUALIZATION_BAND_FREQUENCY.fromInt(4), 3
            )
            audioVoiceEffectsManager.setVoicePitch(0.5)
            voiceEffectButton!!.text = getString(R.string.audio_effect_voice_equalization)
        } else if (voiceEffectIndex > 6) {
            // Remove voice equalization and pitch modification
            audioVoiceEffectsManager.setVoicePitch(1.0)
            audioVoiceEffectsManager.setVoiceEqualization(
                Constants.AUDIO_EQUALIZATION_BAND_FREQUENCY.fromInt(4), 0
            )
            voiceEffectIndex = 0
            voiceEffectButton!!.text = getString(R.string.apply_voice_equalization)
        }
    }
}