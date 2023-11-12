package io.agora.android_reference_app

import android.view.View
import io.agora.multiple_channels_manager.MultipleChannelsManager

class MultipleChannelsActivity : BasicImplementationActivity() {
    private lateinit var multipleChannelsManager: MultipleChannelsManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_basic_implementation

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        multipleChannelsManager = MultipleChannelsManager(this)
        agoraManager = multipleChannelsManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        multipleChannelsManager.joinChannelWithToken()
    }

    fun channelRelay(view: View) {}
}