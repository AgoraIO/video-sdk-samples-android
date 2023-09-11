package io.agora.play_media_manager

import io.agora.rtc2.*
import io.agora.rtc2.IRtcEngineEventHandler.RemoteVideoStats

import io.agora.authentication_manager.AuthenticationManager
import android.content.Context

class PlayMediaManager(context: Context?) : AuthenticationManager(context) {
    private val baseEventHandler: IRtcEngineEventHandler = super.iRtcEngineEventHandler // Reuse the base class event handler
    private var counter = 0 // To control the frequency of messages

    override val iRtcEngineEventHandler: IRtcEngineEventHandler
        get() = object : IRtcEngineEventHandler() {
            override fun onConnectionStateChanged(state: Int, reason: Int) {
                // Occurs when the network connection state changes
                sendMessage(
                    "Connection state changed\n" +
                            "New state: $state\n" +
                            "Reason: $reason"
                )
            }

            override fun onLastmileQuality(quality: Int) {
                // Reports the last-mile network quality of the local user
                (mListener as CallQualityManagerListener).onLastMileQuality(quality)
            }

            override fun onLastmileProbeResult(result: LastmileProbeResult) {
                // Reports the last mile network probe result
                agoraEngine!!.stopLastmileProbeTest()
                // The result object contains the detailed test results that help you
                // manage call quality, for example, the down link bandwidth.
                sendMessage("Available down link bandwidth: " + result.downlinkReport.availableBandwidth)
            }

            override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
                // Reports the last mile network quality of each user in the channel
                (mListener as CallQualityManagerListener).onNetworkQuality(
                    uid, txQuality, rxQuality
                )
            }

            override fun onRtcStats(rtcStats: RtcStats) {
                // Reports the statistics of the current session
                counter += 1
                var msg = ""
                if (counter == 5) msg =
                    rtcStats.users.toString() + " user(s)" else if (counter == 10) {
                    msg = "Packet loss rate: " + rtcStats.rxPacketLossRate
                    counter = 0
                }
                if (msg.isNotEmpty()) sendMessage(msg)
            }

            override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
                // Occurs when the remote video stream state changes
                val msg = "Remote video state changed:\n" +
                        "Uid = $uid\n" +
                        "NewState = $state\n" +
                        "Reason = $reason\n" +
                        "Elapsed = $elapsed"
                sendMessage(msg)
            }

            override fun onRemoteVideoStats(stats: RemoteVideoStats) {
                // Reports the statistics of the video stream sent by each remote user
                (mListener as CallQualityManagerListener).onRemoteVideoStats(
                    stats
                )
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                baseEventHandler.onUserJoined(uid, elapsed)
                (mListener as CallQualityManagerListener).onUserJoined(
                    uid
                )
            }

            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                baseEventHandler.onJoinChannelSuccess(channel, uid, elapsed)
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                baseEventHandler.onUserOffline(uid, reason)
            }

            override fun onTokenPrivilegeWillExpire(token: String) {
                baseEventHandler.onTokenPrivilegeWillExpire(token)
            }
        }
      
    interface CallQualityManagerListener : AgoraManagerListener {
        override fun onMessageReceived(message: String?)
        fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int)
        fun onLastMileQuality(quality: Int)
        fun onUserJoined(uid: Int)
        fun onRemoteVideoStats(stats: RemoteVideoStats?)
    }
}