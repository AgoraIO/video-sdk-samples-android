package com.example.quickstart_base;

import com.example.agora_manager.AgoraManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {
    private AgoraManager agoraManager;
    private final String appId = "9d2498880e934632b38b0a68fa2f1622"; // "<Your app Id>";
    private String channelName = "demo"; // "<your channel name>";
    private final String token = "007eJxTYLhYN/9N5YFZMS1PH1XX/NrLoTflebqLlEz56vAMD3O7dB4FBssUIxNLCwsLg1RLYxMzY6MkY4skg0Qzi7REozRDMyMjw5iSlIZARob1OkHMjAwQCOKzMKSk5uYzMAAAPjwd2A=="; // "<your access token>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        agoraManager = new AgoraManager(this, appId);
        // Set the current product depending on your application
        agoraManager.setCurrentProduct(AgoraManager.ProductName.BROADCAST_STREAMING);
        agoraManager.setVideoFrameLayouts(
                findViewById(R.id.local_video_view_container),
                findViewById(R.id.remote_video_view_container)
        );
        agoraManager.setListener(new AgoraManager.AgoraManagerListener() {
            @Override
            public void onMessageReceived(String message) {
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
            }
        });

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        RadioButton radioButtonBroadcaster = findViewById(R.id.radioButtonBroadcaster);
        RadioButton radioButtonAudience = findViewById(R.id.radioButtonAudience);

        if (agoraManager.getCurrentProduct()==AgoraManager.ProductName.INTERACTIVE_LIVE_STREAMING
            || agoraManager.getCurrentProduct()==AgoraManager.ProductName.BROADCAST_STREAMING) {
            radioGroup.setVisibility(View.VISIBLE);
        } else {
            radioGroup.setVisibility(View.GONE);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            agoraManager.setBroadcasterRole(checkedId == R.id.radioButtonBroadcaster);
        });
    }

    public void joinLeave(View view) {
        Button btnJoinLeave = findViewById(R.id.btnJoinLeave);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);

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