package com.example.authentication_workflow;

import android.content.Context;
import com.example.agora_manager.AgoraManager;

import android.util.Log;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class AgoraManagerAuthenticationWorkflow extends AgoraManager {
    private int tokenRole; // The token role: Broadcaster or Audience
    private String serverUrl = "<Token Server URL>"; // The base URL to your token server, for example, "https://agora-token-service-production-xxxx.up.railway.app".
    private int tokenExpiryTime = 40; // Time in seconds after which the token will expire.

    public AgoraManagerAuthenticationWorkflow(Context context) {
        super(context);

        // Read the server url and expiry time from the config file
        serverUrl = config.optString("serverUrl");
        tokenExpiryTime = config.optInt("tokenExpiryTime",600);
    }

    private void fetchToken(int uid, String channelName, int tokenRole) {
        // Prepare the Url
        String URLString = serverUrl + "/rtc/" + channelName + "/" + tokenRole + "/"
                + "uid" + "/" + uid + "/?expiry=" + tokenExpiryTime;

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
            public void onFailure(Call call, IOException e) {
                Log.e("IOException", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("Token server response received", response.toString());
                    String rtcToken = null;
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        rtcToken = jsonObject.getString("rtcToken");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    setToken(rtcToken);
                }
            }
        });
    }

    void setToken(String token) {
        //token = newValue;
        if (joined) { // Join a channel
            ChannelMediaOptions options = new ChannelMediaOptions();
            // For a Video call, set the channel profile as COMMUNICATION.
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            // Start local preview.
            agoraEngine.startPreview();

            // Join the channel with a token.
            agoraEngine.joinChannel(token, channelName, localUid, options);
        } else { // Already joined, renew the token by calling renewToken
            agoraEngine.renewToken(token);
            sendMessage("Token renewed");
        }
    }

}


