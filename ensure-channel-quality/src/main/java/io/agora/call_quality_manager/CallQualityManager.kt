package io.agora.call_quality_manager

import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.internal.LastmileProbeConfig
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.IRtcEngineEventHandler.RemoteVideoStats

import io.agora.authentication_manager.AuthenticationManager
import android.content.Context
import android.view.SurfaceView
import android.view.View
import java.lang.Exception

class CallQualityManager(context: Context?) : AuthenticationManager(context) {
    private val baseEventHandler: IRtcEngineEventHandler = super.iRtcEngineEventHandler // Reuse the base class event handler
    private var counter = 0 // To control the frequency of messages

    fun startProbeTest() {
        if (agoraEngine == null) setupAgoraEngine()
        // Configure a LastmileProbeConfig instance.
        val config = LastmileProbeConfig()
        // Probe the uplink network quality.
        config.probeUplink = true
        // Probe the down link network quality.
        config.probeDownlink = true
        // The expected uplink bitrate (bps). The value range is [100000,5000000].
        config.expectedUplinkBitrate = 100000
        // The expected down link bitrate (bps). The value range is [100000,5000000].
        config.expectedDownlinkBitrate = 100000
        agoraEngine!!.startLastmileProbeTest(config)
        sendMessage("Running the last mile probe test ...")
        // Test results are reported through the onLastmileProbeResult callback
    }

    override fun setupAgoraEngine(): Boolean {
        try {
            val config = RtcEngineConfig()
            config.mContext = mContext
            config.mAppId = appId
            config.mEventHandler = iRtcEngineEventHandler
            // Configure the log file
            val logConfig = RtcEngineConfig.LogConfig()
            logConfig.fileSizeInKB = 256 // Range 128-1024 Kb
            logConfig.level = Constants.LogLevel.getValue(Constants.LogLevel.LOG_LEVEL_WARN)
            config.mLogConfig = logConfig
            agoraEngine = RtcEngine.create(config)
            /// Enable video mode
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            sendMessage(e.toString())
            return false
        }

        // Enable the dual stream mode
        agoraEngine!!.setDualStreamMode(Constants.SimulcastStreamMode.ENABLE_SIMULCAST_STREAM)
        // If you set the dual stream mode to AUTO_SIMULCAST_STREAM, the low-quality video
        // steam is not sent by default; the SDK automatically switches to low-quality after
        // it receives a request to subscribe to a low-quality video stream.

        // Set an audio profile and an audio scenario.
        agoraEngine!!.setAudioProfile(
            Constants.AUDIO_PROFILE_DEFAULT,
            Constants.AUDIO_SCENARIO_GAME_STREAMING
        )

        // Set the video profile
        val videoConfig = VideoEncoderConfiguration()
        // Set mirror mode
        videoConfig.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_AUTO
        // Set frameRate
        videoConfig.frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_10.value
        // Set bitrate
        videoConfig.bitrate = VideoEncoderConfiguration.STANDARD_BITRATE
        // Set dimensions
        videoConfig.dimensions = VideoEncoderConfiguration.VD_640x360
        // Set orientation mode
        videoConfig.orientationMode =
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
        // Set degradation preference
        videoConfig.degradationPrefer =
            VideoEncoderConfiguration.DEGRADATION_PREFERENCE.MAINTAIN_BALANCED
        // Set compression preference: low latency or quality
        videoConfig.advanceOptions.compressionPreference =
            VideoEncoderConfiguration.COMPRESSION_PREFERENCE.PREFER_LOW_LATENCY
        // Apply the configuration
        agoraEngine!!.setVideoEncoderConfiguration(videoConfig)

        // Start the probe test
        startProbeTest()
        return true
    }

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

    fun startEchoTest(): SurfaceView {
        if (agoraEngine == null) setupAgoraEngine()
        // Set test configuration parameters
        val echoConfig = EchoTestConfiguration()
        echoConfig.enableAudio = true
        echoConfig.enableVideo = true
        echoConfig.channelId = channelName
        echoConfig.intervalInSeconds = 2 // Interval  between recording and playback
        // Set up a SurfaceView
        val localSurfaceView = SurfaceView(mContext)
        localSurfaceView.visibility = View.VISIBLE
        // Call setupLocalVideo with a VideoCanvas having uid set to 0.
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
        echoConfig.view = localSurfaceView

        // Get a token from the server or from the config file
        if (serverUrl.contains("http")) { // A valid server url is available
            // Fetch a token from the server for channelName
            fetchToken(channelName, 0, object : TokenCallback {
                override fun onTokenReceived(rtcToken: String?) {
                    // Set the token in the config
                    echoConfig.token = rtcToken
                    // Start the echo test
                    agoraEngine!!.startEchoTest(echoConfig)
                }

                override fun onError(errorMessage: String) {
                    // Handle the error
                    sendMessage("Error: $errorMessage")
                }
            })
        } else { // use the token from the config.json file
            echoConfig.token = config!!.optString("rtcToken")
            // Start the echo test
            agoraEngine!!.startEchoTest(echoConfig)
        }
        return localSurfaceView
    }

    fun stopEchoTest() {
        agoraEngine!!.stopEchoTest()
        destroyAgoraEngine()
    }

    fun setStreamQuality(remoteUid: Int, highQuality: Boolean) {
        // Set the stream type of the remote video
        if (highQuality) {
            agoraEngine!!.setRemoteVideoStreamType(remoteUid, Constants.VIDEO_STREAM_HIGH)
        } else {
            agoraEngine!!.setRemoteVideoStreamType(remoteUid, Constants.VIDEO_STREAM_LOW)
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