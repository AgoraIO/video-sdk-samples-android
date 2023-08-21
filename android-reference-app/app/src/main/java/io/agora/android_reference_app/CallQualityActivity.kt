package io.agora.android_reference_app

import io.agora.rtc2.IRtcEngineEventHandler.RemoteVideoStats
import io.agora.call_quality_manager.CallQualityManager
import io.agora.call_quality_manager.CallQualityManager.CallQualityManagerListener

import android.graphics.Color
import android.widget.TextView
import android.os.Bundle
import android.view.SurfaceView
import android.widget.FrameLayout
import android.view.Gravity
import android.view.View
import android.widget.Button

class CallQualityActivity : BasicImplementationActivity() {
    private lateinit var callQualityManager: CallQualityManager
    private var btnEchoTest: Button? = null
    private var networkStatus: TextView? = null // For updating the network status
    private var overlayText: TextView? = null
    private var isEchoTestRunning = false // Keeps track of the echo test
    private val baseListener = agoraManagerListener
    
    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_call_quality

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up access to the UI elements
        networkStatus = findViewById(R.id.networkStatus)
        btnEchoTest = findViewById(R.id.btnEchoTest)
    }

    override fun initializeAgoraManager() {
        // Instantiate an object of the callQualityManager class, which is an extension of the AgoraManager
        callQualityManager = CallQualityManager(this)
        agoraManager = callQualityManager

        // Set up a listener for updating the UI
        agoraManager.setListener(object : CallQualityManagerListener {
            override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
                // Use down-link network quality to update the network status
                runOnUiThread { updateNetworkStatus(rxQuality) }
            }

            override fun onLastMileQuality(quality: Int) {
                runOnUiThread { updateNetworkStatus(quality) }
            }

            override fun onUserJoined(uid: Int) {
                if (overlayText == null) setupOverlayText()
            }

            override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
                // Show remote video stats for the video in the main frame
                val selectedUserId = mainFrame.tag as Int
                if (selectedUserId == agoraManager.localUid) {
                    runOnUiThread { overlayText!!.text = "" }
                    return
                }
                if (selectedUserId == stats!!.uid) {
                    val caption = """
                        Uid: ${stats.uid}
                        Renderer frame rate: ${stats.rendererOutputFrameRate}
                        Received bitrate: ${stats.receivedBitrate}
                        Publish duration: ${stats.publishDuration}
                        Frame loss rate: ${stats.frameLossRate}
                        """.trimIndent()
                    runOnUiThread { overlayText!!.text = caption }
                }
            }

            override fun onMessageReceived(message: String?) {
                baseListener.onMessageReceived(message)
            }

            override fun onRemoteUserJoined(remoteUid: Int, surfaceView: SurfaceView?) {
                baseListener.onRemoteUserJoined(remoteUid, surfaceView)
                // Choose low quality when the video plays in a small frame
                callQualityManager.setStreamQuality(remoteUid, false)
            }

            override fun onRemoteUserLeft(remoteUid: Int) {
                // Hide the overlay text
                runOnUiThread { overlayText!!.text = "" }
                baseListener.onRemoteUserLeft(remoteUid)
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                baseListener.onJoinChannelSuccess(channel, uid, elapsed)
                // Disable the echo test button
                btnEchoTest!!.isEnabled = false
            }
        })
    }

    override fun join() {
        callQualityManager.joinChannelWithToken()
    }

    override fun leave() {
        super.leave()
        btnEchoTest!!.isEnabled = true
        overlayText = null
    }

    fun echoTest(view: View) {
        if (!isEchoTestRunning) {
            btnEchoTest!!.setText(R.string.stop_echo_test)
            surfaceViewMain = callQualityManager.startEchoTest()
            mainFrame.addView(surfaceViewMain)
            isEchoTestRunning = true
        } else {
            callQualityManager.stopEchoTest()
            btnEchoTest!!.setText(R.string.start_echo_test)
            mainFrame.removeView(surfaceViewMain)
            isEchoTestRunning = false
        }
        btnJoinLeave.isEnabled = !isEchoTestRunning
    }

    fun setupOverlayText() {
        // Create a new TextView
        overlayText = TextView(this)
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.BOTTOM // Set gravity to bottom left
        overlayText!!.layoutParams = layoutParams
        overlayText!!.textSize = 14f
        overlayText!!.setTextColor(Color.WHITE)
        overlayText!!.setPadding(10, 0, 0, 0)
        // Add the TextView to the FrameLayout
        runOnUiThread { mainFrame.addView(overlayText) }
    }

    override fun swapVideo(frameId: Int) {
        // Switch to high-quality for the remote video going into the main frame
        val smallFrame = findViewById<FrameLayout>(frameId)
        val smallFrameUid = smallFrame.tag as Int
        if (smallFrameUid != agoraManager.localUid) callQualityManager.setStreamQuality(
            smallFrameUid,
            true
        )

        // Switch to low-quality for the remote video going into the small frame
        val mainFrameUid = mainFrame.tag as Int
        if (mainFrameUid != agoraManager.localUid) callQualityManager.setStreamQuality(
            mainFrameUid,
            false
        )

        // Swap the videos
        super.swapVideo(frameId)
    }

    private fun updateNetworkStatus(quality: Int) {
        // Update the UI to indicate the network status
        when (quality) {
            in 1..2 -> networkStatus?.setBackgroundColor(Color.GREEN)
            in 3..4 -> networkStatus?.setBackgroundColor(Color.YELLOW)
            in 5..6 -> networkStatus?.setBackgroundColor(Color.RED)
            else -> networkStatus?.setBackgroundColor(Color.WHITE)
        }
        networkStatus!!.text = quality.toString()
    }
}