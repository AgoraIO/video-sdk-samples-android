package com.example.call_quality;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private AgoraManagerCallQuality agoraManager;
    private final String appId = "9d2498880e934632b38b0a68fa2f1622"; //""<Your app Id>";
    private String channelName = "demo"; // "<your channel name>";
    private final String token = "007eJxTYNjp6aF0+dyMMJu1d23tuo/qBRXUHEus3BEkYlt/ar65OIMCg2WKkYmlhYWFQaqlsYmZsVGSsUWSQaKZRVqiUZqhmZHRqkb3lIZARgbJ1U2MjAwQCOKzMKSk5uYzMAAAuHUc5A=="; //""<your access token>";

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

        agoraManager = new AgoraManagerCallQuality(this, appId);
        agoraManager.setVideoFrameLayouts(
                findViewById(R.id.local_video_view_container),
                findViewById(R.id.remote_video_view_container)
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

        // Start the probe test
        agoraManager.startProbeTest();

        networkStatus = findViewById(R.id.networkStatus);
        echoTestButton = findViewById(R.id.echoTestButton);
    }

    public void joinLeave(View view) {
        Button btnJoinLeave = findViewById(R.id.btnJoinLeave);

        if (!agoraManager.isJoined()) {
            agoraManager.joinChannel(channelName, token);
            btnJoinLeave.setText("Leave");
        } else {
            agoraManager.leaveChannel();
            btnJoinLeave.setText("Join");
        }
    }

    public void echoTest(View view) {
        if (!isEchoTestRunning) {
            echoTestButton.setText("Stop Echo Test");
            agoraManager.startEchoTest(token);
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