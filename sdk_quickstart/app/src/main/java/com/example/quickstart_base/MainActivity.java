package com.example.quickstart_base;

import com.example.agora_manager.AgoraManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private AgoraManager agoraManager;
    private final String appId = "<Your app Id>";
    private String channelName = "<your channel name>";
    private final String token = "<your access token>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        agoraManager = new AgoraManager(this, appId);
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
    }

    public void joinLeave(View view) {
        Button btnJoinLeave = findViewById(R.id.btnJoinLeave);

        if (!agoraManager.isJoined()) {
            int result = agoraManager.joinChannel(channelName, token);
            if (result == 0) btnJoinLeave.setText("Leave");
        } else {
            agoraManager.leaveChannel();
            btnJoinLeave.setText("Join");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}