package com.example.quickstart_base;

import com.example.agora_manager.AgoraManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {
    private AgoraManager agoraManager;
    private final String appId = "9d2498880e934632b38b0a68fa2f1622"; // "<Your app Id>";
    private String channelName = "demo"; // "<your channel name>";
    private final String token = "007eJxTYFCbt4/j3/VMYUtOG9mfZkc/T/y5QbXT6vuB54djreUYfM4rMFimGJlYWlhYGKRaGpuYGRslGVskGSSaWaQlGqUZmhkZ1VwrTWkIZGRgupbNyMgAgSA+C0NKam4+AwMAK6Uevg=="; // "<your access token>";
    private LinearLayout baseLayout;
    private Button btnJoinLeave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the root view of the included layout
        baseLayout = findViewById(R.id.base_layout);
        // Find the widgets inside the included layout using the root view
        btnJoinLeave = baseLayout.findViewById(com.example.agora_manager.R.id.btnJoinLeave);

        agoraManager = new AgoraManager(this, appId);
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

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            agoraManager.setBroadcasterRole(checkedId == com.example.agora_manager.R.id.radioButtonBroadcaster);
        });
    }

    public void joinLeave(View view) {
        RadioGroup radioGroup = findViewById(com.example.agora_manager.R.id.radioGroup);

        if (!agoraManager.isJoined()) {
            int result = agoraManager.joinChannel(channelName, token);
            if (result == 0) {
                btnJoinLeave.setText("Leave");
                if (radioGroup.getVisibility() != View.GONE) radioGroup.setVisibility(View.INVISIBLE);
            }
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