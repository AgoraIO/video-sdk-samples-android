package com.example.authentication_workflow;

import android.content.Context;
import com.example.agora_manager.AgoraManager;

import android.util.Log;
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

    public AgoraManagerAuthenticationWorkflow(Context context) {
        super(context);

        // Read the server url and expiry time from the config file
        serverUrl = config.optString("serverUrl");
        tokenExpiryTime = config.optInt("tokenExpiryTime",300);
    }

    @Override
    protected IRtcEngineEventHandler getIRtcEngineEventHandler() {
        return new IRtcEngineEventHandler() {
            @Override
            // Listen for a remote user joining the channel.
            public void onUserJoined(int uid, int elapsed) {
                sendMessage("Remote user joined " + uid);
                // Save the uid of the remote user.
                remoteUid = uid;

                if (isBroadcaster && (currentProduct == ProductName.INTERACTIVE_LIVE_STREAMING
                        || currentProduct == ProductName.BROADCAST_STREAMING)) {
                    return;
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

    public void fetchToken(String channelName) {
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
                Log.e("IOException", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Extract rtcToken from the response
                    String rtcToken = null;
                    String result = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        rtcToken = jsonObject.getString("rtcToken");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // Use the token to join a channel or to renew an expiring token
                    useToken(rtcToken);
                }
            }
        });
    }

    private void useToken(String token) {
        if (!joined) { // not joined
            // Join the channel with a token.
            joinChannel(channelName, localUid, token);
        } else { // Already joined, renew the token by calling renewToken
            agoraEngine.renewToken(token);
            sendMessage("Expiring token renewed");
        }
    }
}


