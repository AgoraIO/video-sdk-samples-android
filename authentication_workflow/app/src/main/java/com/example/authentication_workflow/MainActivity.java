package com.example.authentication_workflow;

import com.example.agora_manager.AgoraManager;
import com.example.agora_manager.AgoraManagerAuthentication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {
    private AgoraManagerAuthentication agoraManager;
    private Button btnJoinLeave;
    private EditText editChannelName; // To read the channel name from the UI.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the root view of the included layout
        LinearLayout baseLayout = findViewById(R.id.base_layout);
        // Find the widgets inside the included layout using the root view
        btnJoinLeave = baseLayout.findViewById(com.example.agora_manager.R.id.btnJoinLeave);
        // Create an instance of the AgoraManager class
        agoraManager = new AgoraManagerAuthentication(this);
        // Set the current product depending on your application
        agoraManager.setCurrentProduct(AgoraManager.ProductName.VIDEO_CALLING);
        agoraManager.setVideoFrameLayouts(
                baseLayout.findViewById(com.example.agora_manager.R.id.local_video_view_container),
                baseLayout.findViewById(com.example.agora_manager.R.id.remote_video_view_container)
        );
        agoraManager.setListener(new AgoraManager.AgoraManagerListener() {
            @Override
            public void onMessageReceived(String message) {
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }
        });

        RadioGroup radioGroup = findViewById(com.example.agora_manager.R.id.radioGroup);

        if (agoraManager.getCurrentProduct()==AgoraManager.ProductName.INTERACTIVE_LIVE_STREAMING
                || agoraManager.getCurrentProduct()==AgoraManager.ProductName.BROADCAST_STREAMING) {
            radioGroup.setVisibility(View.VISIBLE);
        } else {
            radioGroup.setVisibility(View.GONE);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) ->
                agoraManager.setBroadcasterRole(checkedId == com.example.agora_manager.R.id.radioButtonBroadcaster));

        editChannelName = findViewById(R.id.editChannelName);
        editChannelName.setText(agoraManager.channelName);
    }

    public void joinLeave(View view) {
        RadioGroup radioGroup = findViewById(com.example.agora_manager.R.id.radioGroup);

        if (!agoraManager.isJoined()) {
            String channelName = editChannelName.getText().toString();
            agoraManager.fetchToken(channelName, new AgoraManagerAuthentication.TokenCallback() {
                @Override
                public void onTokenReceived(String rtcToken) {
                    // Handle the received rtcToken
                    agoraManager.joinChannel(channelName, rtcToken);
                }

                @Override
                public void onError(String errorMessage) {
                    // Handle the error
                    System.out.println("Error: " + errorMessage);
                }
            });
            btnJoinLeave.setText("Leave");
        } else {
            agoraManager.leaveChannel();
            btnJoinLeave.setText("Join");
            if (radioGroup.getVisibility() != View.GONE) radioGroup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}