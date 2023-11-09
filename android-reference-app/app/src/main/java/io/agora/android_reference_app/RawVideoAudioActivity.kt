package io.agora.android_reference_app

import io.agora.raw_video_audio_manager.RawVideoAudioManager

class RawVideoAudioActivity : BasicImplementationActivity() {
    private lateinit var rawVideoAudioManager: RawVideoAudioManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_basic_implementation

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        rawVideoAudioManager = RawVideoAudioManager(this)
        agoraManager = rawVideoAudioManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        rawVideoAudioManager.joinChannelWithToken()
    }
}