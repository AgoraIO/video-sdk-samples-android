package com.example.authentication_workflow;

import android.content.Context;
import com.example.agora_manager.AgoraManager;

import android.view.View;
import androidx.annotation.NonNull;
import io.agora.rtc2.IRtcEngineEventHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class AgoraManagerAuthenticationWorkflow extends AgoraManager {
    private final String serverUrl; // The base URL to your token server
    private final int tokenExpiryTime; // Time in seconds after which the token will expire.

    // Token Callback interface
    public interface TokenCallback {
        void onTokenReceived(String rtcToken);
        void onError(String errorMessage);
    }

    public AgoraManagerAuthenticationWorkflow(Context context) {
        super(context);

        // Read the server url and expiry time from the config file
        serverUrl = config.optString("serverUrl");
        tokenExpiryTime = config.optInt("tokenExpiryTime",300);
    }

    @Override
    protected IRtcEngineEventHandler getIRtcEngineEventHandler() {
        return new IRtcEngineEventHandler() {

            // Listen for the event that the token is about to expire
            @Override
            public void onTokenPrivilegeWillExpire(String token) {
                sendMessage("Token expiring...");
                // Get a new token
                fetchToken(channelName, new AgoraManagerAuthenticationWorkflow.TokenCallback() {
                    @Override
                    public void onTokenReceived(String rtcToken) {
                        // Use the token to renew
                        agoraEngine.renewToken(rtcToken);
                        sendMessage("Expiring token renewed");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Handle the error
                        sendMessage("Error: " + errorMessage);
                    }
                });
                super.onTokenPrivilegeWillExpire(token);
            }

            @Override
            // Listen for a remote user joining the channel.
            public void onUserJoined(int uid, int elapsed) {
                sendMessage("Remote user joined " + uid);
                // Save the uid of the remote user.
                remoteUid = uid;

                if (isBroadcaster && (currentProduct == ProductName.INTERACTIVE_LIVE_STREAMING
                        || currentProduct == ProductName.BROADCAST_STREAMING)) {
                    // Do nothing
                } else {
                    // Set the remote video view for the new user.
                    setupRemoteVideo();
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
                activity.runOnUiThread(() -> remoteSurfaceView.setVisibility(View.GONE));
            }
        };
    }

    public void fetchToken(String channelName, TokenCallback callback) {
        int tokenRole = isBroadcaster ? 1 : 2;
        // Prepare the Url
        String URLString = serverUrl + "/rtc/" + channelName + "/" + tokenRole + "/"
                + "uid" + "/" + localUid + "/?expiry=" + tokenExpiryTime;

        OkHttpClient client = new OkHttpClient();

        // Instantiate the RequestQueue.
        Request request = new Request.Builder()
                .url(URLString)
                .header("Content-Type", "application/json; charset=UTF-8")
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("IOException: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Extract rtcToken from the response
                    String rtcToken = null;
                    try {
                        String result = response.body().string();
                        JSONObject jsonObject = new JSONObject(result);
                        rtcToken = jsonObject.getString("rtcToken");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onTokenReceived("Token request failed");
                    }
                    // Return the token through a callback
                    callback.onTokenReceived(rtcToken);
                } else {
                    callback.onTokenReceived("Token request failed");
                }
            }
        });
    }

}


