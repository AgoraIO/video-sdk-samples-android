package com.example.agora_manager;

import android.content.Context;

import androidx.annotation.NonNull;
import io.agora.rtc2.IRtcEngineEventHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class AgoraManagerAuthentication extends AgoraManager {
    protected final String serverUrl; // The base URL to your token server
    private final int tokenExpiryTime; // Time in seconds after which the token will expire.
    private final IRtcEngineEventHandler baseEventHandler;

    // Callback interface to receive the http response from an async token request
    public interface TokenCallback {
        void onTokenReceived(String rtcToken);
        void onError(String errorMessage);
    }

    public AgoraManagerAuthentication(Context context) {
        super(context);
        // Read the server url and expiry time from the config file
        serverUrl = config.optString("serverUrl");
        tokenExpiryTime = config.optInt("tokenExpiryTime",600);
        baseEventHandler = super.getIRtcEngineEventHandler();
    }

    @Override
    protected IRtcEngineEventHandler getIRtcEngineEventHandler() {
        return new IRtcEngineEventHandler() {

            // Listen for the event that the token is about to expire
            @Override
            public void onTokenPrivilegeWillExpire(String token) {
                sendMessage("Token is about to expire");
                // Get a new token
                fetchToken(channelName, new AgoraManagerAuthentication.TokenCallback() {
                    @Override
                    public void onTokenReceived(String rtcToken) {
                        // Use the token to renew
                        agoraEngine.renewToken(rtcToken);
                        sendMessage("Token renewed");
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
                baseEventHandler.onUserJoined(uid, elapsed);
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                baseEventHandler.onJoinChannelSuccess(channel,uid,elapsed);
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                baseEventHandler.onUserOffline(uid, reason);
            }
        };
    }

    public void fetchToken(String channelName, TokenCallback callback) {
        fetchToken(channelName, localUid, callback);
    }

    public void fetchToken(String channelName, int uid, TokenCallback callback) {
        int tokenRole = isBroadcaster ? 1 : 2;
        // Prepare the Url
        String URLString = serverUrl + "/rtc/" + channelName + "/" + tokenRole + "/"
                + "uid" + "/" + uid + "/?expiry=" + tokenExpiryTime;

        OkHttpClient client = new OkHttpClient();

        // Create a request
        Request request = new Request.Builder()
                .url(URLString)
                .header("Content-Type", "application/json; charset=UTF-8")
                .get()
                .build();

        // Send the async http request
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            // Receive the response in a callback
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Extract rtcToken from the response
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String rtcToken = jsonObject.getString("rtcToken");
                        // Return the token to the code that called fetchToken
                        callback.onTokenReceived(rtcToken);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onError("Invalid token response");
                    }
                } else {
                    callback.onError("Token request failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("IOException: " + e);
            }
        });
    }

    public int joinChannelWithToken() {
        return joinChannelWithToken(channelName);
    }

    public int joinChannelWithToken(String channelName) {
        if (isValidURL(serverUrl)) { // A valid server url is available
            // Fetch a token from the server for channelName
            fetchToken(channelName, new AgoraManagerAuthentication.TokenCallback() {
                @Override
                public void onTokenReceived(String rtcToken) {
                    // Handle the received rtcToken
                    joinChannel(channelName, rtcToken);
                }

                @Override
                public void onError(String errorMessage) {
                    // Handle the error
                    sendMessage("Error: " + errorMessage);
                }
            });
            return 0;
        } else { // use the token from the config.json file
            String token = config.optString("rtcToken");
            return  joinChannel(channelName, token);
        }
    }

    public static boolean isValidURL(String urlString) {
        try {
            // Attempt to create a URL object from the given string
            URL url = new URL(urlString);
            // Check if the URL's protocol and host are not empty
            return url.getProtocol() != null && url.getHost() != null;
        } catch (MalformedURLException e) {
            // The given string is not a valid URL
            return false;
        }
    }
}


