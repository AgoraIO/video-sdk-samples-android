package io.agora.android_reference_app

import io.agora.custom_video_audio_manager.CustomVideoAudioManager

class CustomVideoAudioActivity : BasicImplementationActivity() {
    private lateinit var customVideoAudioManager: CustomVideoAudioManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_basic_implementation

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        customVideoAudioManager = CustomVideoAudioManager(this)
        agoraManager = customVideoAudioManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        customVideoAudioManager.joinChannelWithToken()
    }
}