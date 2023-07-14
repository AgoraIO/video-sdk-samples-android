package com.example.call_quality;

import com.example.authentication_workflow.AgoraManagerAuthenticationWorkflow;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private AgoraManagerCallQuality agoraManager;

    private LinearLayout baseLayout;
    private Button btnJoinLeave;
    private TextView networkStatus; // For updating the network status
    private boolean isEchoTestRunning = false; // Keeps track of the echo test
    private Button echoTestButton;

    private void updateNetworkStatus(int quality) {
        if (quality > 0 && quality < 3) networkStatus.setBackgroundColor(Color.GREEN);
        else if (quality <= 4) networkStatus.setBackgroundColor(Color.YELLOW);
        else if (quality <= 6) networkStatus.setBackgroundColor(Color.RED);
        else networkStatus.setBackgroundColor(Color.WHITE);
    }

    public void setStreamQuality(View view) {
        agoraManager.switchStreamQuality();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the root view of the included layout
        baseLayout = findViewById(R.id.base_layout);
        // Find the widgets inside the included layout using the root view
        btnJoinLeave = baseLayout.findViewById(com.example.agora_manager.R.id.btnJoinLeave);
        btnJoinLeave.setOnClickListener(this::joinLeave);
        FrameLayout remoteFrameLayout = baseLayout.findViewById(com.example.agora_manager.R.id.remote_video_view_container);

        agoraManager = new AgoraManagerCallQuality(this);
        // Set the current product depending on your application
        agoraManager.setCurrentProduct(AgoraManager.ProductName.VIDEO_CALLING);
        agoraManager.setVideoFrameLayouts(
                baseLayout.findViewById(com.example.agora_manager.R.id.local_video_view_container),
                baseLayout.findViewById(com.example.agora_manager.R.id.remote_video_view_container)
        );
        agoraManager.setListener(new AgoraManagerCallQuality.AgoraManagerCallQualityListener() {
            @Override
            public void onMessageReceived(String message) {
                showMessage(message);
            }

            @Override
            public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
                // Use down-link network quality to update the network status
                runOnUiThread(() -> updateNetworkStatus(rxQuality));
            }

            @Override
            public void onLastMileQuality(int quality) {
                runOnUiThread(() -> updateNetworkStatus(quality));
            }
        });

        RadioGroup radioGroup = findViewById(com.example.agora_manager.R.id.radioGroup);

        // Manage Broadcaster and Audience roles in Interactive live streaming
        if (agoraManager.getCurrentProduct()==AgoraManager.ProductName.INTERACTIVE_LIVE_STREAMING
                || agoraManager.getCurrentProduct()==AgoraManager.ProductName.BROADCAST_STREAMING) {
            radioGroup.setVisibility(View.VISIBLE);
        } else {
            radioGroup.setVisibility(View.GONE);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            agoraManager.setBroadcasterRole(checkedId == com.example.agora_manager.R.id.radioButtonBroadcaster);
        });

        // Switch stream quality when a user taps the remote video
        remoteFrameLayout.setOnClickListener(this::setStreamQuality);

        // Start the probe test
        agoraManager.startProbeTest();

        networkStatus = findViewById(R.id.networkStatus);
        echoTestButton = findViewById(R.id.echoTestButton);
    }

    public void joinLeave(View view) {
        RadioGroup radioGroup = findViewById(com.example.agora_manager.R.id.radioGroup);

        if (!agoraManager.isJoined()) {
            int result = agoraManager.joinChannel();
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

    public void echoTest(View view) {
        if (!isEchoTestRunning) {
            echoTestButton.setText("Stop Echo Test");
            agoraManager.startEchoTest();
            isEchoTestRunning = true;
        } else {
            agoraManager.stopEchoTest();
            echoTestButton.setText("Start Echo Test");
            isEchoTestRunning = false;
        }
    }

    private void showMessage(String message) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}