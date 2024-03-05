package io.agora.android_reference_app

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import io.agora.multiple_channels_manager.MultipleChannelsManager

class MultipleChannelsActivity : BasicImplementationActivity() {
    private lateinit var multipleChannelsManager: MultipleChannelsManager
    private var channelMediaButton: Button? = null
    private var secondChannelButton: Button? = null

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_multiple_channels

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        multipleChannelsManager = MultipleChannelsManager(this)
        agoraManager = multipleChannelsManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        forceShowRemoteViews = true
        super.onCreate(savedInstanceState)

        channelMediaButton = findViewById(R.id.mediaRelayButton)
        secondChannelButton = findViewById(R.id.secondChannelButton)
        findViewById<View>(R.id.smallVideosView).visibility = View.VISIBLE
    }

    override fun join() {
        multipleChannelsManager.joinChannelWithToken()
    }

    override fun handleEngineEvent(eventName: String, eventArgs: Map<String, Any>) {

        when (eventName) {
            "onChannelMediaRelayStateChanged" -> {
                when (eventArgs["state"]) {
                    // This example shows toast messages when the relay state changes,
                    // a production level app needs to handle state change properly.
                    1 -> {
                        showMessage("Channel media relay connecting.")
                        runOnUiThread { channelMediaButton?.text = getString(R.string.connecting_) }
                    }
                    2 -> {
                        showMessage("Channel media relay running.")
                        runOnUiThread {
                            channelMediaButton?.text = getString(R.string.stop_channel_media_relay)
                        }
                    }
                    3 -> {
                        if (eventArgs["code"] == 2) {
                            showMessage("No server response. Make sure that channel media relay service has been enabled for your project.")
                        } else {
                            showMessage("Channel media relay failure. Error code: ${eventArgs["code"]}")
                        }
                        runOnUiThread {
                            channelMediaButton?.text = getString(R.string.start_channel_media_relay)
                        }
                    }
                    else -> {

                    }
                }
            }
            "onJoinChannelSuccess2" -> {
                runOnUiThread {
                    secondChannelButton?.text = getString(R.string.leave_second_channel)
                }
            }
            "onLeaveChannel2" -> {
                runOnUiThread {
                    secondChannelButton?.text = getString(R.string.join_second_channel)
                }
            }
            "onUserJoined2" -> {
                // Display remote video
                showMessage("A remote user joined the second channel")
                showRemoteVideo(eventArgs["uid"] as Int, eventArgs["surfaceView"] as SurfaceView?)
            }
            "onUserOffline2" -> {
                showMessage("A remote user left the second channel")
                removeUserView(eventArgs["uid"] as Int)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun channelRelay(view: View) {
        multipleChannelsManager.channelRelay()
    }

    @Suppress("UNUSED_PARAMETER")
    fun joinSecondChannel(view: View) {
        multipleChannelsManager.joinSecondChannel()
    }
}
