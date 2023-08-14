package io.agora.agora_manager

import android.Manifest
import android.app.Activity
import android.content.Context
import io.agora.agora_manager.AgoraManager.AgoraManagerListener
import org.json.JSONObject
import io.agora.agora_manager.AgoraManager.ProductName
import io.agora.agora_manager.AgoraManager
import io.agora.agora_manager.R
import org.json.JSONException
import android.view.SurfaceView
import io.agora.rtc2.video.VideoCanvas
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc2.*
import java.io.IOException
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.HashSet

open class AgoraManager(context: Context) {
    // The reference to the Android activity you use for video calling
    protected val activity: Activity
    protected val mContext: Context

    // The RTCEngine instance
    protected var agoraEngine: RtcEngine? = null

    // The event handler for agoraEngine events
    protected var mListener: AgoraManagerListener? = null

    // Configuration parameters from the config.json file
    protected var config: JSONObject?

    // Your App ID from Agora console
    protected val appId: String

    // The name of the channel to join
    var channelName: String

    // UID of the local user
    var localUid: Int

    // An object to store uids of remote users
    var remoteUids = HashSet<Int>()

    // Status of the video call
    var isJoined = false
        private set
    var isBroadcaster = true

    // The Agora product to test. Choose the product depending on your application
    var currentProduct = ProductName.VIDEO_CALLING
    fun setBroadcasterRole(isBroadcaster: Boolean) {
        this.isBroadcaster = isBroadcaster
    }

    enum class ProductName {
        VIDEO_CALLING, VOICE_CALLING, INTERACTIVE_LIVE_STREAMING, BROADCAST_STREAMING
    }

    init {
        config = readConfig(context)
        appId = config!!.optString("appId")
        channelName = config!!.optString("channelName")
        localUid = config!!.optInt("uid")
        mContext = context
        activity = mContext as Activity
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(activity, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }
    }

    fun setListener(mListener: AgoraManagerListener?) {
        this.mListener = mListener
    }

    fun readConfig(context: Context): JSONObject? {
        try {
            val inputStream = context.resources.openRawResource(R.raw.config)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, StandardCharsets.UTF_8)
            return JSONObject(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    // Create a SurfaceView object
    val localVideo: SurfaceView
        get() {
            // Create a SurfaceView object
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
            return localSurfaceView
        }

    protected fun setupRemoteVideo(remoteUid: Int) {
        // Create a new SurfaceView
        val remoteSurfaceView = SurfaceView(mContext)
        remoteSurfaceView.setZOrderMediaOverlay(true)
        // Create and set up a VideoCanvas
        val videoCanvas = VideoCanvas(
            remoteSurfaceView,
            VideoCanvas.RENDER_MODE_FIT, remoteUid
        )
        agoraEngine!!.setupRemoteVideo(videoCanvas)
        // Set the visibility
        remoteSurfaceView.visibility = View.VISIBLE
        // Notify the UI to display the video
        mListener!!.onRemoteUserJoined(remoteUid, remoteSurfaceView)
    }

    protected open fun setupAgoraEngine(): Boolean {
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
            agoraEngine.enableVideo()
        } catch (e: Exception) {
            sendMessage(e.toString())
            return false
        }
        return true
    }

    fun joinChannel(): Int {
        // Use channelName and token from the config file
        val token = config!!.optString("rtcToken")
        return joinChannel(channelName, token)
    }

    fun joinChannel(channelName: String, token: String?): Int {
        // Check that necessary permissions have been granted
        if (!checkSelfPermission()) {
            sendMessage("Permissions were not granted")
            return -1
        }
        this.channelName = channelName

        // Create an RTCEngine instance
        if (agoraEngine == null) setupAgoraEngine()
        val options = ChannelMediaOptions()
        if (currentProduct == ProductName.VIDEO_CALLING) {
            // For a Video call, set the channel profile as COMMUNICATION.
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            isBroadcaster = true
        } else {
            // For Live Streaming and Broadcast streaming,
            // set the channel profile as LIVE_BROADCASTING.
            options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            if (currentProduct == ProductName.BROADCAST_STREAMING) {
                // Set Low latency for Broadcast streaming
                if (!isBroadcaster) options.audienceLatencyLevel =
                    Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
            }
        }

        // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
        if (isBroadcaster) { // Broadcasting Host or Video-calling client
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            // Start local preview.
            agoraEngine!!.startPreview()
        } else { // Audience
            options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
        }

        // Join the channel with a token.
        agoraEngine!!.joinChannel(token, channelName, localUid, options)
        return 0
    }

    fun leaveChannel() {
        if (!isJoined) {
            sendMessage("Join a channel first")
        } else {
            // To leave a channel, call the `leaveChannel` method
            agoraEngine!!.leaveChannel()
            sendMessage("You left the channel")

            // Set the `joined` status to false
            isJoined = false
            // Destroy the engine instance
            destroyAgoraEngine()
        }
    }

    protected fun destroyAgoraEngine() {
        // Release the RtcEngine instance to free up resources
        RtcEngine.destroy()
        agoraEngine = null
    }// Set joined status to true.

    // Save the uid of the local user.
// Set the remote video view for the new user.// Remote video does not need to be rendered// Save the uid of the remote user.
    // Listen for a remote user joining the channel.
    protected open val iRtcEngineEventHandler: IRtcEngineEventHandler?
        protected get() = object : IRtcEngineEventHandler() {
            // Listen for a remote user joining the channel.
            override fun onUserJoined(uid: Int, elapsed: Int) {
                sendMessage("Remote user joined $uid")
                // Save the uid of the remote user.
                remoteUids.add(uid)
                if (isBroadcaster && (currentProduct == ProductName.INTERACTIVE_LIVE_STREAMING
                            || currentProduct == ProductName.BROADCAST_STREAMING)
                ) {
                    // Remote video does not need to be rendered
                } else {
                    // Set the remote video view for the new user.
                    setupRemoteVideo(uid)
                }
            }

            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                // Set joined status to true.
                isJoined = true
                sendMessage("Joined Channel $channel")
                // Save the uid of the local user.
                localUid = uid
                mListener!!.onJoinChannelSuccess(channel, uid, elapsed)
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                sendMessage("Remote user offline $uid $reason")
                remoteUids.remove(uid)
                mListener!!.onRemoteUserLeft(uid)
            }

            override fun onError(err: Int) {
                when (err) {
                    ErrorCode.ERR_TOKEN_EXPIRED -> sendMessage("Your token has expired")
                    ErrorCode.ERR_INVALID_TOKEN -> sendMessage("Your token is invalid")
                    else -> sendMessage("Error code: $err")
                }
            }
        }

    protected fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            mContext,
            REQUESTED_PERMISSIONS[0]
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    mContext,
                    REQUESTED_PERMISSIONS[1]
                ) == PackageManager.PERMISSION_GRANTED
    }

    interface AgoraManagerListener {
        fun onMessageReceived(message: String?)
        fun onRemoteUserJoined(remoteUid: Int, surfaceView: SurfaceView?)
        fun onRemoteUserLeft(remoteUid: Int)
        fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int)
    }

    protected fun sendMessage(message: String?) {
        mListener!!.onMessageReceived(message)
    }

    companion object {
        protected const val PERMISSION_REQ_ID = 22
        protected val REQUESTED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
    }
}