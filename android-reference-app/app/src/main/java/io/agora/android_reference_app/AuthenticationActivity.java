package io.agora.android_reference_app;

import io.agora.agora_manager.AgoraManager;
import io.agora.authentication_manager.AuthenticationManager;

import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.EditText;

public class AuthenticationActivity extends BasicImplementationActivity {
    private AuthenticationManager authenticationManager;
    private EditText editChannelName; // To read the channel name from the UI.
    private EditText editServerUrl; // To read the server Url from the UI.

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_authentication;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        editChannelName = findViewById(R.id.editChannelName);
        editChannelName.setText(agoraManager.channelName);

        editServerUrl = findViewById(R.id.editServerUrl);
        editServerUrl.setText(authenticationManager.serverUrl);
    }

    @Override
    protected void initializeAgoraManager() {
        authenticationManager = new AuthenticationManager(this);
        agoraManager = authenticationManager;

        // Set the current product depending on your application
        agoraManager.setCurrentProduct(AgoraManager.ProductName.VIDEO_CALLING);
        // Set up a listener for updating the UI
        agoraManager.setListener(getAgoraManagerListener());
    }

    @Override
    protected void join() {
        // Read the channel name
        String channelName = editChannelName.getText().toString();
        // Read the authentication server URL
        authenticationManager.serverUrl = editServerUrl.getText().toString();

        // Fetch a token from the server using an async call
        authenticationManager.fetchToken(channelName, new AuthenticationManager.TokenCallback() {
            @Override
            public void onTokenReceived(String rtcToken) {
                // Join a channel using the token
                agoraManager.joinChannel(channelName, rtcToken);
                // Start local video
                showLocalVideo();
                runOnUiThread(()->
                    btnJoinLeave.setText(R.string.leave)
                );
            }

            @Override
            public void onError(String errorMessage) {
                // Fetch token failed
                System.out.println("Error: " + errorMessage);
            }
        });
    }
}