package io.agora.android_reference_app

import android.view.View
import io.agora.audio_voice_effects_manager.AudioVoiceEffectsManager

class AudioVoiceEffectsActivity : BasicImplementationActivity() {
    private lateinit var audioVoiceEffectsManager: AudioVoiceEffectsManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_audio_voice_effects

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

    fun audioMixing(view: View) {}
    fun playSoundEffect(view: View) {}
    fun applyVoiceEffect(view: View) {}
}