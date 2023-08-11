package io.agora.android_reference_app;

import io.agora.agora_manager.AgoraManager;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.RadioGroup;

import java.util.HashMap;
import java.util.Map;

public class BasicImplementationActivity extends AppCompatActivity {
    protected AgoraManager agoraManager;
    protected LinearLayout baseLayout;
    protected Button btnJoinLeave;
    protected FrameLayout mainFrame;
    protected LinearLayout containerLayout;
    protected RadioGroup radioGroup;
    protected Map<Integer, FrameLayout> videoFrameMap;

    protected void initializeAgoraManager() {
        agoraManager = new AgoraManager(this);
        // Set the current product depending on your application
        agoraManager.setCurrentProduct(AgoraManager.ProductName.VIDEO_CALLING);
        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener);
    }

    protected int getLayoutResourceId() {
        return R.layout.activity_basic_implementation; // Default layout resource ID for base activity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        // Find the root view of the included layout
        baseLayout = findViewById(R.id.base_layout);
        // Find the widgets inside the included layout using the root view
        btnJoinLeave = baseLayout.findViewById(R.id.btnJoinLeave);
        // Find the main video frame
        mainFrame = findViewById(R.id.main_video_container);
        // Find the multi video container layout
        containerLayout = findViewById(R.id.containerLayout);
        // Find the radio group for role
        radioGroup = findViewById(R.id.radioGroup);
        videoFrameMap = new HashMap<>();

        // Create an instance of the AgoraManager class
        initializeAgoraManager();

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

    protected void join() {
        int result = agoraManager.joinChannel();
        if (result == 0) {
            btnJoinLeave.setText("Leave");
            if (radioGroup.getVisibility() != View.GONE) radioGroup.setVisibility(View.INVISIBLE);
        }

        showLocalVideo();
    }

    protected void showLocalVideo() {
        if (agoraManager.isBroadcaster) {
            runOnUiThread(()->{
                // Display the local video
                SurfaceView localVideoSurfaceView = agoraManager.getLocalVideo();
                mainFrame.addView(localVideoSurfaceView);
                videoFrameMap.put(0, mainFrame);
                mainFrame.setTag(0);
            });
        }
    }

    protected void leave() {
        agoraManager.leaveChannel();
        btnJoinLeave.setText("Join");
        if (radioGroup.getVisibility() != View.GONE) radioGroup.setVisibility(View.VISIBLE);
        // Clear the video container
        for (int remoteUid : agoraManager.remoteUids) {
            FrameLayout frameLayoutToRemove = findViewById(remoteUid);
            // Remove the FrameLayout from the video container
            containerLayout.removeView(frameLayoutToRemove);
        }
        mainFrame.removeAllViews();
    }

    public void joinLeave(View view) {
        if (!agoraManager.isJoined()) {
            join();
        } else {
            leave();
        }
    }

    protected void showMessage(String message) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    AgoraManager.AgoraManagerListener agoraManagerListener = (new AgoraManager.AgoraManagerListener() {
        @Override
        public void onMessageReceived(String message) {
            showMessage(message);
        }

        @Override
        public void onRemoteUserJoined(int remoteUid, SurfaceView surfaceView) {
            runOnUiThread(() -> {
                // Create a new FrameLayout programmatically
                FrameLayout remoteFrameLayout = new FrameLayout(getApplicationContext());
                // Add the SurfaceView to the FrameLayout
                remoteFrameLayout.addView(surfaceView);
                // Set the layout parameters for the new FrameLayout
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        400,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                layoutParams.setMargins(6,6,6,6);
                // Set the background color for the new FrameLayout
                remoteFrameLayout.setBackgroundResource(R.color.dark_gray);
                // Set the id for the new FrameLayout
                remoteFrameLayout.setId(View.generateViewId());
                remoteFrameLayout.setTag(remoteUid);
                videoFrameMap.put(remoteUid, remoteFrameLayout);

                remoteFrameLayout.setOnClickListener(videoClickListener);
                // Add the new FrameLayout to the parent LinearLayout
                containerLayout.addView(remoteFrameLayout,layoutParams);
            });
        }

        @Override
        public void onRemoteUserLeft(int remoteUid) {
            runOnUiThread(() -> {
                FrameLayout frameLayout = videoFrameMap.get(remoteUid);
                if (frameLayout.getId() == mainFrame.getId()) {
                    swapVideo(videoFrameMap.get(0).getId());
                    frameLayout = videoFrameMap.get(remoteUid);
                }
                // Remove the FrameLayout from the LinearLayout
                containerLayout.removeView(frameLayout);
            });
        }
    });

    View.OnClickListener videoClickListener = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // A small video frame was clicked
            swapVideo(v.getId());

        }
    });

    protected void swapVideo(int frameId) {
        runOnUiThread(() -> {
            // Swap the videos in the small frame and the main frame
            FrameLayout smallFrame = findViewById(frameId);

            SurfaceView surfaceViewMain = (SurfaceView) mainFrame.getChildAt(0);
            SurfaceView surfaceViewSmall = (SurfaceView) smallFrame.getChildAt(0);

            mainFrame.removeView(surfaceViewMain);
            smallFrame.removeView(surfaceViewSmall);

            mainFrame.addView(surfaceViewSmall);
            smallFrame.addView(surfaceViewMain);

            // Swap tags
            int tag = (int) mainFrame.getTag();
            mainFrame.setTag(smallFrame.getTag());
            smallFrame.setTag(tag);

            // Update the videoFrameMap
            videoFrameMap.put((int) smallFrame.getTag(), smallFrame);
            videoFrameMap.put((int) mainFrame.getTag(), mainFrame);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}