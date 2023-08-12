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
    private Button btnEchoTest;
    private final AgoraManager.AgoraManagerListener baseListener = getAgoraManagerListener();
    private TextView overlayText;

    private void updateNetworkStatus(int quality) {
        if (quality > 0 && quality < 3) networkStatus.setBackgroundColor(Color.GREEN);
        else if (quality <= 4) networkStatus.setBackgroundColor(Color.YELLOW);
        else if (quality <= 6) networkStatus.setBackgroundColor(Color.RED);
        else networkStatus.setBackgroundColor(Color.WHITE);

        networkStatus.setText(Integer.toString(quality));
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_call_quality;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkStatus = findViewById(R.id.networkStatus);
        btnEchoTest = findViewById(R.id.btnEchoTest);

        // Start the probe test
        callQualityManager.startProbeTest();
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
                if (overlayText == null ) setupOverlayText();
            }

            @Override
            public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
                int selectedUserId = (int) mainFrame.getTag();
                if (selectedUserId == agoraManager.localUid) {
                    runOnUiThread(() -> overlayText.setText(""));
                    return;
                }
                if (selectedUserId == stats.uid)  {
                    String caption = "Uid: " + stats.uid
                            + "\nRenderer frame rate: " + stats.rendererOutputFrameRate
                            + "\nReceived bitrate: " + stats.receivedBitrate
                            + "\nPublish duration: " + stats.publishDuration
                            + "\nFrame loss rate: " + stats.frameLossRate;
                    runOnUiThread(() -> overlayText.setText(caption)
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
                // Choose low quality when the video plays in a small frame
                callQualityManager.setStreamQuality(remoteUid, false);
            }

            @Override
            public void onRemoteUserLeft(int remoteUid) {
                runOnUiThread(() -> overlayText.setText(""));
                baseListener.onRemoteUserLeft(remoteUid);
            }
        });
    }

    @Override
    protected void join() {
        int result = callQualityManager.joinChannelWithToken();
        if (result == 0) {
            // Start local video
            showLocalVideo();
            btnJoinLeave.setText(R.string.leave);
            btnEchoTest.setEnabled(false);
            if (radioGroup.getVisibility() != View.GONE) radioGroup.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void leave() {
        super.leave();
        btnEchoTest.setEnabled(true);
        overlayText = null;
    }

    public void echoTest(View view) {
        if (!isEchoTestRunning) {
            btnEchoTest.setText(R.string.stop_echo_test);
            surfaceViewMain = callQualityManager.startEchoTest();
            mainFrame.addView(surfaceViewMain);
            isEchoTestRunning = true;
        } else {
            callQualityManager.stopEchoTest();
            btnEchoTest.setText(R.string.start_echo_test);
            mainFrame.removeView(surfaceViewMain);
            isEchoTestRunning = false;
        }
        btnJoinLeave.setEnabled(!isEchoTestRunning);
    }

    public void setupOverlayText() {
        // Create a new TextView
        overlayText = new TextView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.BOTTOM; // Set gravity to bottom left
        overlayText.setLayoutParams(layoutParams);
        overlayText.setTextSize(14);
        overlayText.setTextColor(Color.WHITE);
        overlayText.setPadding(10, 0, 0, 0);
        // Add the TextView to the FrameLayout
        runOnUiThread(() ->
                mainFrame.addView(overlayText));
    }

    @Override
    protected void swapVideo(int frameId) {
        // Switch to high-quality for the remote video going into the main frame
        FrameLayout smallFrame = findViewById(frameId);
        int smallFrameUid = (int) smallFrame.getTag();
        if (smallFrameUid != agoraManager.localUid)
            callQualityManager.setStreamQuality(smallFrameUid, true);

        // Switch to low-quality for the remote video going into the small frame
        int mainFrameUid = (int) mainFrame.getTag();
        if (mainFrameUid != agoraManager.localUid)
            callQualityManager.setStreamQuality(mainFrameUid, false);

        // Swap the videos
        super.swapVideo(frameId);
    }
}