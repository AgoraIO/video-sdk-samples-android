package io.agora.android_reference_app

import io.agora.media_stream_encryption_manager.MediaStreamEncryptionManager

class MediaStreamEncryptionActivity : BasicImplementationActivity() {
    private lateinit var mediaStreamEncryptionManager: MediaStreamEncryptionManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_basic_implementation

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        mediaStreamEncryptionManager = MediaStreamEncryptionManager(this)
        agoraManager = mediaStreamEncryptionManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        mediaStreamEncryptionManager.joinChannelWithToken()
    }
}