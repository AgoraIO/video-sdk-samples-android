package io.agora.android_reference_app

import io.agora.cloud_proxy_manager.CloudProxyManager

class CloudProxyActivity : BasicImplementationActivity() {
    private lateinit var cloudProxyManager: CloudProxyManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_basic_implementation

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        cloudProxyManager = CloudProxyManager(this)
        agoraManager = cloudProxyManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        cloudProxyManager.joinChannelWithToken()
    }
}