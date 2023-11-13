package io.agora.multiple_channels_manager

import android.content.Context
import android.view.SurfaceView
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.*
import io.agora.rtc2.video.ChannelMediaInfo
import io.agora.rtc2.video.ChannelMediaRelayConfiguration
import io.agora.rtc2.video.VideoCanvas


class MultipleChannelsManager(context: Context?) : AuthenticationManager(context) {
    private val baseEventHandler: IRtcEngineEventHandler? // To extend the event handler from the base class

    // Channel media relay variables
    private var destinationChannelName: String // Name of the destination channel
    private var destinationChannelToken: String // Access token for the destination channel
    private var destinationChannelUid = 0 // User ID that the user uses in the destination channel
    private var sourceChannelToken: String // Access token for the source channel, Generate using channelName and uid = 0
    private var mediaRelaying = false

    // Multi channel streaming variables
    private lateinit var agoraEngineEx: RtcEngineEx
    private var rtcSecondConnection: RtcConnection? = null
    private var secondChannelName: String // Name of the second channel"
    private var secondChannelUid = 0 // Uid for the second channel
    private var secondChannelToken: String  // Access token for the second channel
    private var isSecondChannelJoined = false // Track connection state of the second channel

    init {
        baseEventHandler = super.iRtcEngineEventHandler
        destinationChannelName = config!!.optString("destinationChannelName")
        destinationChannelUid = config!!.optInt("destinationChannelUid")
        destinationChannelToken = config!!.optString("destinationChannelToken")
        sourceChannelToken = config!!.optString("sourceChannelToken")

        secondChannelName = config!!.optString("secondChannelName")
        secondChannelUid = config!!.optInt("secondChannelUid")
        secondChannelToken = config!!.optString("secondChannelToken")
    }

    override fun setupAgoraEngine(): Boolean {
        try {
            // Set the engine configuration
            val config = RtcEngineConfig()
            config.mContext = mContext
            config.mAppId = appId
            // Assign an event handler to receive engine callbacks
            config.mEventHandler = iRtcEngineEventHandler

            // Create an RtcEngine instance
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine!!.enableVideo()

        } catch (e: Exception) {
            sendMessage(e.toString())
            return false
        }
        return true
    }

    fun channelRelay() {
        if (mediaRelaying) {
            agoraEngine!!.stopChannelMediaRelay()
        } else {
            // Configure the source channel information.
            val srcChannelInfo = ChannelMediaInfo(channelName, sourceChannelToken, 0)
            val mediaRelayConfiguration = ChannelMediaRelayConfiguration()
            mediaRelayConfiguration.setSrcChannelInfo(srcChannelInfo)

            // Configure the destination channel information.
            val destChannelInfo = ChannelMediaInfo(destinationChannelName, destinationChannelToken, destinationChannelUid)
            mediaRelayConfiguration.setDestChannelInfo(destinationChannelName, destChannelInfo)

            // Start relaying media streams across channels
            agoraEngine!!.startOrUpdateChannelMediaRelay(mediaRelayConfiguration)
        }
    }

    // Listen for the event that a token is about to expire
    override val iRtcEngineEventHandler: IRtcEngineEventHandler
        get() = object : IRtcEngineEventHandler() {
            override fun onChannelMediaRelayStateChanged(state: Int, code: Int) {
                if (state == 2) {
                    mediaRelaying = true
                } else if (state == 3) {
                    mediaRelaying = false
                }
                // Inform the UI
                val eventArgs = mutableMapOf<String, Any>()
                eventArgs["state"] = state
                eventArgs["code"] = code
                mListener?.onEngineEvent("onChannelMediaRelayStateChanged", eventArgs)
            }

            // Listen for the event that the token is about to expire
            override fun onTokenPrivilegeWillExpire(token: String) {
                baseEventHandler!!.onTokenPrivilegeWillExpire(token)
            }

            // Reuse events handlers from the base class
            override fun onUserJoined(uid: Int, elapsed: Int) {
                baseEventHandler!!.onUserJoined(uid, elapsed)
            }

            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                baseEventHandler!!.onJoinChannelSuccess(channel, uid, elapsed)
                handleOnJoinChannelSuccess(channel, uid, elapsed)
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                baseEventHandler!!.onUserOffline(uid, reason)
            }

            override fun onConnectionStateChanged(state: Int, reason: Int) {
                connectionStateChanged(state, reason)
            }
        }

    fun joinSecondChannel() {
        // Create an RtcEngineEx instance
        agoraEngineEx = RtcEngineEx.create(mContext, appId, secondChannelEventHandler) as RtcEngineEx
        // By default, the video module is disabled, call enableVideo to enable it.
        agoraEngineEx.enableVideo()

        if (isSecondChannelJoined) {
            agoraEngineEx.leaveChannelEx(rtcSecondConnection)
        } else {
            val mediaOptions = ChannelMediaOptions()
            if (!isBroadcaster) { // Audience Role
                mediaOptions.autoSubscribeAudio = true
                mediaOptions.autoSubscribeVideo = true
                mediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            } else { // Host Role
                mediaOptions.publishCameraTrack = true
                mediaOptions.publishMicrophoneTrack = true
                mediaOptions.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
                mediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            }
            rtcSecondConnection = RtcConnection()
            rtcSecondConnection!!.channelId = secondChannelName
            rtcSecondConnection!!.localUid = secondChannelUid

            if (isValidURL(serverUrl)) {
                fetchToken(channelName, secondChannelUid, object : TokenCallback {
                    override fun onTokenReceived(rtcToken: String?) {
                        // Handle the received rtcToken
                        if (rtcToken != null) secondChannelToken = rtcToken
                        agoraEngineEx.joinChannelEx(
                            secondChannelToken,
                            rtcSecondConnection,
                            mediaOptions,
                            secondChannelEventHandler
                        )
                    }

                    override fun onError(errorMessage: String) {
                        // Handle the error
                        sendMessage("Error: $errorMessage")
                    }
                })
            } else {
                agoraEngineEx.joinChannelEx(
                    secondChannelToken,
                    rtcSecondConnection,
                    mediaOptions,
                    secondChannelEventHandler
                )
            }
        }
    }

    // Callbacks for the second channel
    private val secondChannelEventHandler: IRtcEngineEventHandler =
        object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                isSecondChannelJoined = true
                sendMessage("Joined channel $secondChannelName, uid: $uid")
                val eventArgs = mutableMapOf<String, Any>()
                eventArgs["channel"] = channel
                eventArgs["uid"] = uid
                mListener?.onEngineEvent("onJoinChannelSuccess2", eventArgs)
            }

            override fun onLeaveChannel(stats: RtcStats) {
                isSecondChannelJoined = false
                sendMessage("Left the channel $secondChannelName")
                val eventArgs = mutableMapOf<String, Any>()
                eventArgs["stats"] = stats
                mListener?.onEngineEvent("onLeaveChannel2", eventArgs)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                sendMessage(String.format("user %d joined!", uid))

                // Create surfaceView for remote video
                val remoteSurfaceView = SurfaceView(mContext)
                remoteSurfaceView.setZOrderMediaOverlay(true)

                // Setup remote video to render
                agoraEngineEx.setupRemoteVideoEx(
                    VideoCanvas(
                        remoteSurfaceView,
                        VideoCanvas.RENDER_MODE_HIDDEN, uid
                    ), rtcSecondConnection
                )

                val eventArgs = mutableMapOf<String, Any>()
                eventArgs["uid"] = uid
                eventArgs["surfaceView"] = remoteSurfaceView
                mListener?.onEngineEvent("onUserJoined2", eventArgs)
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                val eventArgs = mutableMapOf<String, Any>()
                eventArgs["uid"] = uid
                mListener?.onEngineEvent("onUserOffline2", eventArgs)
            }
        }
}