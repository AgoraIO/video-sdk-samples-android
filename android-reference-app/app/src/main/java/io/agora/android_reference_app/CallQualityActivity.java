package io.agora.android_reference_app;

import io.agora.agora_manager.AgoraManager;
import io.agora.call_quality_manager.CallQualityManager;
import io.agora.rtc2.IRtcEngineEventHandler;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CallQualityActivity extends BasicImplementationActivity {
    private CallQualityManager callQualityManager;

    private TextView networkStatus; // For updating the network status
    private boolean isEchoTestRunning = false; // Keeps track of the echo test
    private Button echoTestButton;
    private final AgoraManager.AgoraManagerListener baseListener = getAgoraManagerListener();
    private TextView remoteStatsText;
    private void updateNetworkStatus(int quality) {
        if (quality > 0 && quality < 3) networkStatus.setBackgroundColor(Color.GREEN);
        else if (quality <= 4) networkStatus.setBackgroundColor(Color.YELLOW);
        else if (quality <= 6) networkStatus.setBackgroundColor(Color.RED);
        else networkStatus.setBackgroundColor(Color.WHITE);
    }

    public void setStreamQuality(View view) {
        //callQualityManager.switchStreamQuality();
    }

     @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_call_quality;
    }

    @Override
    protected void initializeAgoraManager() {
        callQualityManager = new CallQualityManager(this);
        agoraManager = callQualityManager;

        // Set the current product depending on your application
        agoraManager.setCurrentProduct(AgoraManager.ProductName.VIDEO_CALLING);
        // Set up a listener for updating the UI
        agoraManager.setListener(new CallQualityManager.CallQualityManagerListener() {

            @Override
            public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
                // Use down-link network quality to update the network status
                runOnUiThread(() -> updateNetworkStatus(rxQuality));
            }

            @Override
            public void onLastMileQuality(int quality) {
                runOnUiThread(() -> updateNetworkStatus(quality));
            }

            @Override
            public void onUserJoined(int uid) {

                //setupOverlayText();

            }

            @Override
            public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
                if (agoraManager.remoteUids.contains(stats.uid) ) {
                    String caption = "Renderer frame rate: " + stats.rendererOutputFrameRate
                            + "\nReceived bitrate: " + stats.receivedBitrate
                            + "\nPublish duration: " + stats.publishDuration
                            + "\nFrame loss rate: " + stats.frameLossRate;
                    runOnUiThread(() -> remoteStatsText.setText(caption)
                    );
                }
            }

            @Override
            public void onMessageReceived(String message) {
                baseListener.onMessageReceived(message);
            }

            @Override
            public void onRemoteUserJoined(int remoteUid, SurfaceView surfaceView) {
                baseListener.onRemoteUserJoined(remoteUid, surfaceView);
            }

            @Override
            public void onRemoteUserLeft(int remoteUid) {
                baseListener.onRemoteUserLeft(remoteUid);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_quality);

        networkStatus = findViewById(R.id.networkStatus);
        echoTestButton = findViewById(R.id.echoTestButton);

        // Start the probe test
        callQualityManager.startProbeTest();
    }


    @Override
    protected void join() {
        int result = callQualityManager.joinChannelWithToken();
        if (result == 0) {
            // Start local video
            showLocalVideo();
//            runOnUiThread(()-> {
                btnJoinLeave.setText(R.string.leave);
  //          });
            if (radioGroup.getVisibility() != View.GONE) radioGroup.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void leave() {
        super.leave();
        removeOverlayText();
    }

    public void echoTest(View view) {
        if (!isEchoTestRunning) {
            echoTestButton.setText(R.string.stop_echo_test);
            callQualityManager.startEchoTest();
            isEchoTestRunning = true;
        } else {
            callQualityManager.stopEchoTest();
            echoTestButton.setText(R.string.start_echo_test);
            isEchoTestRunning = false;
        }
    }

    public void setupOverlayText() {
        // Create a new TextView
        remoteStatsText = new TextView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.BOTTOM; // Set gravity to bottom left
        remoteStatsText.setLayoutParams(layoutParams);
        remoteStatsText.setTextSize(14);
        remoteStatsText.setTextColor(Color.WHITE);
        remoteStatsText.setPadding(10, 0, 0, 0);
        // Add the TextView to the FrameLayout
        runOnUiThread(() ->
                mainFrame.addView(remoteStatsText));
    }

    private void removeOverlayText() {
        runOnUiThread(() ->
                mainFrame.removeView(remoteStatsText));
        // Dispose the TextView
        remoteStatsText = null;
    }
}