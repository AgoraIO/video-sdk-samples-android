package io.agora.agora_manager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.view.SurfaceView;
import android.view.View;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.ChannelMediaOptions;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class AgoraManager {
    // The reference to the Android activity you use for video calling
    protected final Activity activity;
    protected final Context mContext;
    // The RTCEngine instance
    protected RtcEngine agoraEngine;
    // The event handler for agoraEngine events
    protected AgoraManagerListener mListener;
    // Configuration parameters from the config.json file
    protected JSONObject config;
    // Your App ID from Agora console
    protected final String appId;
    // The name of the channel to join
    public String channelName;
    // UID of the local user
    public int localUid;
    // An object to store uids of remote users
    public HashSet<Integer> remoteUids = new HashSet<>();
    // Status of the video call
    protected boolean joined = false;
    // The Agora product to test
    protected ProductName currentProduct = ProductName.VIDEO_CALLING;
    public boolean isBroadcaster = true;

    public void setBroadcasterRole(boolean isBroadcaster) {
        this.isBroadcaster = isBroadcaster;
    }

    public enum ProductName {
        VIDEO_CALLING,
        VOICE_CALLING,
        INTERACTIVE_LIVE_STREAMING,
        BROADCAST_STREAMING
    }

    public void setCurrentProduct(ProductName product) {
        currentProduct = product;
    }

    public ProductName getCurrentProduct() {
        return currentProduct;
    }

    protected static final int PERMISSION_REQ_ID = 22;
    protected static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };

    public AgoraManager(Context context) {
        config = readConfig(context);
        appId = config.optString("appId");
        channelName = config.optString("channelName");
        localUid = config.optInt("uid");

        mContext = context;
        activity = (Activity) mContext;

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(activity, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
    }

    public void setListener(AgoraManagerListener mListener) {
        this.mListener = mListener;
    }

    public JSONObject readConfig(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            return new JSONObject(jsonString);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void setupLocalVideo() {
        // Run code on the UI thread as the code modifies the UI
     /*   activity.runOnUiThread(() -> {
            // Create a SurfaceView object
            localSurfaceView = new SurfaceView(mContext);
            // Add it as a child to a FrameLayout.
            localFrameLayout.addView(localSurfaceView);
            localSurfaceView.setVisibility(View.VISIBLE);
            // Call setupLocalVideo with a VideoCanvas having uid set to 0.
            agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        });
      */
    }

    public SurfaceView getLocalVideo() {
        // Create a SurfaceView object
        SurfaceView localSurfaceView = new SurfaceView(mContext);
        // Add it as a child to a FrameLayout.
        //localFrameLayout.addView(localSurfaceView);
        localSurfaceView.setVisibility(View.VISIBLE);
        // Call setupLocalVideo with a VideoCanvas having uid set to 0.
        agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        return localSurfaceView;
    }

    protected void setupRemoteVideo (int remoteUid) {
        // Create a new SurfaceView
        SurfaceView remoteSurfaceView = new SurfaceView(mContext);
        remoteSurfaceView.setZOrderMediaOverlay(true);
        // Create and set up a VideoCanvas
        VideoCanvas videoCanvas = new VideoCanvas(remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT, remoteUid);
        agoraEngine.setupRemoteVideo(videoCanvas);
        // Set the visibility
        remoteSurfaceView.setVisibility(View.VISIBLE);
        // Notify the UI to display the video
        mListener.onRemoteUserJoined(remoteUid, remoteSurfaceView);
    }

    protected boolean setupAgoraEngine() {
        try {
            // Set the engine configuration
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = mContext;
            config.mAppId = appId;
            // Assign an event handler to receive engine callbacks
            config.mEventHandler = getIRtcEngineEventHandler();
            // Create an RtcEngine instance
            agoraEngine = RtcEngine.create(config);
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine.enableVideo();
        } catch (Exception e) {
            sendMessage(e.toString());
            return false;
        }
        return true;
    }

    public boolean isJoined () {
        return joined;
    }

    public int joinChannel() {
        // Use channelName and token from the config file
        String token = config.optString("rtcToken");
        return  joinChannel(channelName, token);
    }

    public int joinChannel(String channelName, String token) {
        // Check that necessary permissions have been granted
        if (!checkSelfPermission()) {
            sendMessage("Permissions were not granted");
            return -1;
        }

        this.channelName = channelName;

        // Create an RTCEngine instance
        if (agoraEngine == null) setupAgoraEngine();
            ChannelMediaOptions options = new ChannelMediaOptions();
            if (currentProduct == ProductName.VIDEO_CALLING) {
                // For a Video call, set the channel profile as COMMUNICATION.
                options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
                isBroadcaster = true;
            } else {
                // For Live Streaming and Broadcast streaming,
                // set the channel profile as LIVE_BROADCASTING.
                options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
                if (currentProduct == ProductName.BROADCAST_STREAMING) {
                   // Set Low latency for Broadcast streaming
                   if (!isBroadcaster)
                       options.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY;
                }
            }

            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            if (isBroadcaster) { // Broadcasting Host or Video-calling client
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                // Display LocalSurfaceView.
                // setupLocalVideo();
                // Start local preview.
                agoraEngine.startPreview();
            } else { // Audience
                options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
            }

            // Join the channel with a token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            // If a user ID is not assigned or set to 0, the SDK assigns a random number and returns it in the onJoinChannelSuccess callback.
            agoraEngine.joinChannel(token, channelName, localUid, options);

        return 0;
    }

    public void leaveChannel() {
        if (!joined) {
            sendMessage("Join a channel first");
        } else {
            // To leave a channel, call the `leaveChannel` method
            agoraEngine.leaveChannel();
            sendMessage("You left the channel");

            // Set the `joined` status to false
            joined = false;
            // Destroy the engine instance
            destroyAgoraEngine();
        }
    }

    protected void destroyAgoraEngine() {
        // Release the RtcEngine instance to free up resources
        RtcEngine.destroy();
        agoraEngine = null;
    }

    protected IRtcEngineEventHandler getIRtcEngineEventHandler() {

        return new IRtcEngineEventHandler() {
            @Override
            // Listen for a remote user joining the channel.
            public void onUserJoined(int uid, int elapsed) {
                sendMessage("Remote user joined " + uid);
                // Save the uid of the remote user.
                remoteUids.add(uid);

                if (isBroadcaster && (currentProduct == ProductName.INTERACTIVE_LIVE_STREAMING
                    || currentProduct == ProductName.BROADCAST_STREAMING)) {
                    // Remote video does not need to be rendered
                } else {
                    // Set the remote video view for the new user.
                    setupRemoteVideo(uid);
                }
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                // Set joined status to true.
                joined = true;
                sendMessage("Joined Channel " + channel);
                // Save the uid of the local user.
                localUid = uid;
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                sendMessage("Remote user offline " + uid + " " + reason);
                remoteUids.remove(uid);
                mListener.onRemoteUserLeft(uid);
            }
        };
    }

    protected boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(mContext, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    public interface AgoraManagerListener {
        void onMessageReceived(String message);
        void onRemoteUserJoined(int remoteUid, SurfaceView surfaceView);
        void onRemoteUserLeft(int remoteUid);
    }

    protected void sendMessage(String message) {
        mListener.onMessageReceived(message);
    }
}
