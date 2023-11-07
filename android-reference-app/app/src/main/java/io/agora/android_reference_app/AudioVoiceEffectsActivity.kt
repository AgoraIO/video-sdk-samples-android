package io.agora.android_reference_app

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import io.agora.audio_voice_effects_manager.AudioVoiceEffectsManager

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
        speakerphoneSwitch!!.setOnCheckedChangeListener { buttonView, isChecked ->

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

    fun audioMixing(view: View) {
        val button: Button =  findViewById(R.id.AudioMixingButton)
        audioPlaying = !audioPlaying

        if (audioPlaying) {
            button.setText(getString(R.string.stop_audio_mixing))
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
        if (soundEffectStatus == 0) { // Stopped
            audioVoiceEffectsManager.playEffect(soundEffectId, soundEffectFilePath)
            playEffectButton?.text = getString(R.string.pause_audio_effect)
            soundEffectStatus = 1
        } else if (soundEffectStatus == 1) { // Playing
            audioVoiceEffectsManager.pauseEffect(soundEffectId)
            soundEffectStatus = 2
            playEffectButton?.text = getString(R.string.resume_audio_effect)
        } else if (soundEffectStatus == 2) { // Paused
            audioVoiceEffectsManager.resumeEffect(soundEffectId)
            soundEffectStatus = 1
            playEffectButton?.text  =getString(R.string.pause_audio_effect)
        }
    }

    fun applyVoiceEffect(view: View) {

    }
}