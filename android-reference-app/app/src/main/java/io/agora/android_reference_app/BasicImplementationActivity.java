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
        agoraManager.setListener(getAgoraManagerListener());
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
            showLocalVideo();
            if (radioGroup.getVisibility() != View.GONE) radioGroup.setVisibility(View.INVISIBLE);
        }
    }

    protected void showLocalVideo() {
        if (agoraManager.isBroadcaster) {
            runOnUiThread(()->{
                // Get the SurfaceView for the local video
                SurfaceView localVideoSurfaceView = agoraManager.getLocalVideo();
                // Add te SurfaceView to a FrameLayout
                mainFrame.addView(localVideoSurfaceView);
                // Associate the FrameLayout
                videoFrameMap.put(agoraManager.localUid, mainFrame);
                mainFrame.setTag(agoraManager.localUid);
            });
        }
    }

    protected void leave() {
        agoraManager.leaveChannel();

        btnJoinLeave.setText("Join");
        if (radioGroup.getVisibility() != View.GONE) radioGroup.setVisibility(View.VISIBLE);

        // Clear the video containers
        containerLayout.removeAllViews();
        mainFrame.removeAllViews();
        videoFrameMap.clear();
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

    protected AgoraManager.AgoraManagerListener getAgoraManagerListener() {
    return  (new AgoraManager.AgoraManagerListener() {
        @Override
        public void onMessageReceived(String message) {
            showMessage(message);
        }

        @Override
        public void onRemoteUserJoined(int remoteUid, SurfaceView surfaceView) {
            runOnUiThread(() -> {
                // Create a new FrameLayout
                FrameLayout remoteFrameLayout = new FrameLayout(getApplicationContext());
                // Add the SurfaceView to the FrameLayout
                remoteFrameLayout.addView(surfaceView);
                // Set the layout parameters for the new FrameLayout
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        400,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                layoutParams.setMargins(6,6,6,6);
                // Set the id for the new FrameLayout
                remoteFrameLayout.setId(View.generateViewId());
                // Associate the remoteUid with the FrameLayout for use in swapping
                remoteFrameLayout.setTag(remoteUid);
                videoFrameMap.put(remoteUid, remoteFrameLayout);
                // Set an onclick listener for video swapping
                remoteFrameLayout.setOnClickListener(videoClickListener);
                // Add the new FrameLayout to the parent LinearLayout
                containerLayout.addView(remoteFrameLayout,layoutParams);
            });
        }

        @Override
        public void onRemoteUserLeft(int remoteUid) {
            runOnUiThread(() -> {
                // Get the FrameLayout in which the video was displayed
                FrameLayout frameLayoutOfUser = videoFrameMap.get(remoteUid);

                // If the video was in the main frame swap it with the local frame
                if (frameLayoutOfUser.getId() == mainFrame.getId()) {
                    swapVideo(videoFrameMap.get(agoraManager.localUid).getId());
                }

                // Remove the FrameLayout from the LinearLayout
                FrameLayout frameLayoutToDelete = videoFrameMap.get(remoteUid);
                containerLayout.removeView(frameLayoutToDelete);
                videoFrameMap.remove(remoteUid);
            });
        }
    });
    }


    View.OnClickListener videoClickListener = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // A small video frame was clicked
            swapVideo(v.getId());
        }
    });

    protected void swapVideo(int frameId) {
        // Swap the  video in the small frame with the main frame
        runOnUiThread(() -> {
            // Swap the videos in the small frame and the main frame
            FrameLayout smallFrame = findViewById(frameId);

            // Get the SurfaceView in the frames
            SurfaceView surfaceViewMain = (SurfaceView) mainFrame.getChildAt(0);
            SurfaceView surfaceViewSmall = (SurfaceView) smallFrame.getChildAt(0);

            // Swap the SurfaceViews
            mainFrame.removeView(surfaceViewMain);
            smallFrame.removeView(surfaceViewSmall);
            mainFrame.addView(surfaceViewSmall);
            smallFrame.addView(surfaceViewMain);

            // Swap the FrameLayout tags
            int tag = (int) mainFrame.getTag();
            mainFrame.setTag(smallFrame.getTag());
            smallFrame.setTag(tag);

            // Update the videoFrameMap to keep track of videos
            videoFrameMap.put((int) smallFrame.getTag(), smallFrame);
            videoFrameMap.put((int) mainFrame.getTag(), mainFrame);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}