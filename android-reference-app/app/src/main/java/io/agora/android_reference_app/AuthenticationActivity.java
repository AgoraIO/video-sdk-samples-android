package io.agora.android_reference_app;

import io.agora.agora_manager.AgoraManager;
import io.agora.authentication_manager.AuthenticationManager;

import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.EditText;

public class AuthenticationActivity extends BasicImplementationActivity {
    private AuthenticationManager authenticationManager;
    //private Button btnJoinLeave;
    private EditText editChannelName; // To read the channel name from the UI.
    private EditText editServerUrl; // To read the server Url from the UI.

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_authentication; // Default layout resource ID for base activity
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
        agoraManager.setListener(agoraManagerListener);
    }

    @Override
    protected void join() {
        String channelName = editChannelName.getText().toString();
        authenticationManager.serverUrl = editServerUrl.getText().toString();
        authenticationManager.fetchToken(channelName, new AuthenticationManager.TokenCallback() {
            @Override
            public void onTokenReceived(String rtcToken) {
                // Handle the received rtcToken
                agoraManager.joinChannel(channelName, rtcToken);
                showLocalVideo();
            }

            @Override
            public void onError(String errorMessage) {
                // Handle the error
                System.out.println("Error: " + errorMessage);
            }
        });
        btnJoinLeave.setText(R.string.leave);
    }
}