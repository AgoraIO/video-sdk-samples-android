# SDK quickstart

Video Calling enables one-to-one or small-group video chat connections with smooth, jitter-free streaming video. Agora’s Video SDK makes it easy to embed real-time video chat into web, mobile, and native apps.

Thanks to Agora’s intelligent and global Software Defined Real-time Network ([Agora SD-RTN™](https://docs.agora.io/en/video-calling/overview/core-concepts#agora-sd-rtn)), you can rely on the highest available video and audio quality.

This page provides a sample project with best-practice code that illustrates the integration of high-quality, low-latency Video Calling features into an app using Video SDK.

## Understand the tech

This section explains how Video Calling works in an app. Best practice is to implement the following steps:

- *Set a token*: A token is a computer-generated string that authenticates a user when an app joins a channel. For testing purposes in this guide, you generate a temporary token from Agora Console. In a production environment, you need to create an authentication server and retrieve the token from it. See [Implement the authentication workflow](https://docs.agora.io/en/video-calling/develop/authentication-workflow) and [Token generators](https://docs.agora.io/en//video-calling/develop/integrate-token-generation) for details.

- *Join a channel*: Call methods to create an Agora Engine instance and join a channel. A token is generated for a single channel. The apps that pass tokens generated using the same app ID and channel name join the same channel.

- *Send and receive video and audio in the channel*: All users send and receive video and audio streams from all users in the channel.


![Video Calling Web UIKit](./images/video-call.png)

## Prerequisites

In order to get and run the SDK quickstart project sample, you must have:

* Installed [Git](https://git-scm.com/downloads).
- [Android Studio](https://developer.android.com/studio) 4.1 or higher.
- Android SDK API Level 24 or higher.
- A mobile device that runs Android 4.1 or higher.

- An Agora [account](https://docs.agora.io/en/video-calling/reference/manage-agora-account#create-an-agora-account) and [project](https://docs.agora.io/en/video-calling/reference/manage-agora-account#create-an-agora-project).
- A computer with Internet access.

    Ensure that no firewall is blocking your network communication.

## Project setup

To get the sample project, take the following steps:

1. Clone the [Video SDK samples Git repository](https://github.com/AgoraIO/video-sdk-samples-android) to your development machine:

    ```bash
    git clone https://github.com/AgoraIO/video-sdk-samples-android.git
    ```

1. Launch Android Studio. To open the sample project, select **Open...** from the **File** menu and navigate to the `sdk_quickstart` folder under `video-sdk-samples-android/Docs-Examples/`. Android Studio loads the project and Gradle sync finishes downloading the dependencies.

1. Connect a physical or virtual Android device to your development environment.

## Implementing a client for Video Calling

When a user attempts to join the channel, you initialize Agora Engine and connect to it.


The following workflow demonstrates these core features:

![image](./images/video-call-logic-android.svg)


This section highlights essential code from the sample project to demonstrate how you can implement the basic Video Calling API call sequence in your own project. The code highlighted on this page is stored in `agora_helper/java/com/example/agora_helper/AgoraManager.java`. This file defines the `AgoraManager` class that encapsulates the `RTCEngine` instance and its core functionality. The code performs the following functions:

1. **Import Video SDK classes and interfaces**

    ``` java
    import io.agora.rtc2.RtcEngine;
    import io.agora.rtc2.RtcEngineConfig;
    import io.agora.rtc2.Constants;
    import io.agora.rtc2.IRtcEngineEventHandler;
    import io.agora.rtc2.video.VideoCanvas;
    import io.agora.rtc2.ChannelMediaOptions;
    ```

2.  **Declare variables to create an Agora Engine instance and join a channel**

    ``` java
    // The reference to the Android activity you use for video calling
    protected final Activity activity;
    protected final Context mContext;
    // The RTCEngine instance
    protected RtcEngine agoraEngine;
    // The event handler for agoraEngine events
    protected AgoraManagerListener mListener;
    // Your App ID from Agora console
    protected final String appId;
    // The name of the channel to join
    protected String channelName;
    // UIDs of the local and remote users
    protected int localUid = 0, remoteUid = 0;
    // Status of the video call
    protected boolean joined = false;
    // Reference to FrameLayouts in your UI for rendering local and remote videos
    protected FrameLayout localFrameLayout, remoteFrameLayout;
    //SurfaceView to render local video in a Container.
    protected SurfaceView localSurfaceView;
    //SurfaceView to render Remote video in a Container.
    protected SurfaceView remoteSurfaceView;
    ```

3.  **Set up  Agora Engine**

    The following code in the `AgoraManager` class uses the application context and the app ID to create an instance named `agoraEngine` and sets up an event handler for it.

    ``` java
    protected boolean setupAgoraEngine() {
        try {
            // Set the engine configuration
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = mContext;
            config.mAppId = appId;
            // Assign an event handler to receive engine callbacks
            config.mEventHandler = getIRtcEngineEventHandler();
            // Create an RtcEngine instance 
            agoraEngine = RtcEngine.create(config);
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine.enableVideo();
        } catch (Exception e) {
            sendMessage(e.toString());
            return false;
        }
        return true;
    }
    ```

4.  **Handle and respond to  Agora Engine events**

    ``` java
    protected IRtcEngineEventHandler getIRtcEngineEventHandler() {

        return new IRtcEngineEventHandler() {
            @Override
            // Listen for a remote user joining the channel.
            public void onUserJoined(int uid, int elapsed) {
                sendMessage("Remote user joined " + uid);
                // Save the uid of the remote user.
                remoteUid = uid;

                // Set the remote video view for the new user.
                setupRemoteVideo();
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                // Set joined status to true.
                joined = true;
                sendMessage("Joined Channel " + channel);
                // Save the uid of the local user.
                localUid = uid;
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                sendMessage("Remote user offline " + uid + " " + reason);
                activity.runOnUiThread(() -> remoteSurfaceView.setVisibility(View.GONE));
            }
        };
    }
    ```

5.  **Render video from a remote user in the channel**

    ``` java
    protected void setupRemoteVideo () {
        // Run code on the UI thread as the code modifies the UI
        activity.runOnUiThread(() -> {
            // Create a new SurfaceView
            remoteSurfaceView = new SurfaceView(mContext);
            remoteSurfaceView.setZOrderMediaOverlay(true);
            // Add the SurfaceView to a FrameLayout in the UI
            remoteFrameLayout.addView(remoteSurfaceView);
            // Create and set up a VideoCanvas
            VideoCanvas videoCanvas = new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT,
                    Constants.VIDEO_MIRROR_MODE_ENABLED, remoteUid);
            agoraEngine.setupRemoteVideo(videoCanvas);
            // Set the visibility 
            remoteSurfaceView.setVisibility(View.VISIBLE);
        });
    }
    ```

6.  **Render video from the local user in the channel**

    ``` java
    protected void setupLocalVideo() {
        // Run code on the UI thread as the code modifies the UI
        activity.runOnUiThread(() -> {
            // Create a SurfaceView object
            localSurfaceView = new SurfaceView(mContext);
            // Add it as a child to a FrameLayout.
            localFrameLayout.addView(localSurfaceView);
            localSurfaceView.setVisibility(View.VISIBLE);
            // Call setupLocalVideo with a VideoCanvas having uid set to 0.
            agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        });
    }
    ```

7.  **Join a channel to start Video Calling**

    ``` java
    public int joinChannel(String channelName, String token) {
        this.channelName = channelName;

        // Create an RTCEngine instance 
        if (agoraEngine == null) setupAgoraEngine();
        // Check that necessary permissions have been granted
        if (checkSelfPermission()) {
            ChannelMediaOptions options = new ChannelMediaOptions();
            // For a Video call, set the channel profile as COMMUNICATION.
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            // Display LocalSurfaceView.
            setupLocalVideo();
            // Start local preview.
            agoraEngine.startPreview();
            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            // If a user ID is not assigned or set to 0, the SDK assigns a random number and returns it in the onJoinChannelSuccess callback.
            agoraEngine.joinChannel(token, channelName, localUid, options);
        } else {
            sendMessage("Permissions were not granted");
            return -1;
        }
        return 0;
    }
    ```

8.  **Leave the channel when the local user ends the call**

    ``` java
    public void leaveChannel() {
        if (!joined) {
            sendMessage("Join a channel first");
        } else {
            // To leave a channel, call the `leaveChannel` method
            agoraEngine.leaveChannel();
            sendMessage("You left the channel");

            activity.runOnUiThread(() -> {
                // Hide local and remote SurfaceViews
                if (remoteSurfaceView != null)  remoteSurfaceView.setVisibility(View.GONE);
                if (localSurfaceView != null) localSurfaceView.setVisibility(View.GONE);
            });
            // Set the `joined` status to false
            joined = false;
            // Destroy the engine instance
            destroyAgoraEngine();
        }
    }    
    ```

1.  **Clean up the resources used by the app**

    ``` java
    protected void destroyAgoraEngine() {
        // Release the RtcEngine instance to free up resources
        RtcEngine.destroy();
        agoraEngine = null;
    }
    ```

## Test your implementation

This section explains how to run the sample project and see the corresponding features in an app. Best practice is to run this project on a physical mobile device, as some simulators may not support the full features of this project.

1. [Generate a temporary token](https://docs.agora.io/en/video-calling/reference/manage-agora-account#generate-a-temporary-token) in Agora Console.

2. In your browser, navigate to the <Link target="_blank" to="{{Global.DEMO_BASIC_VIDEO_CALL_URL}}">Agora web demo</Link> and update _App ID_, _Channel_, and _Token_ with the values for your temporary token, then click **Join**.

    3.  In Android Studio:

        1. In `app/java/com/example/quickstart_base/MainActivity.java` of the sample project, update `appId`, `channelName`, and `token` with the values for your temporary token.

        1.  Click **Run app**. A moment later you see the project installed on your device. If this is the first time you run the project, you need to grant microphone and camera access to your app.

    4. Click **Join** to start a call. Now, you can see yourself on the device screen and talk to the remote user using your app.

## Reference

This section contains information that completes the information in this page, or points you to documentation that explains other aspects to this product.

- [Downloads](https://docs.agora.io/en/video-calling/reference/downloads) shows you how to install Video SDK manually.

- For a more complete example, see the <a href="https://github.com/AgoraIO/API-Examples/tree/main/Android">open source  Video Calling example project</a> on GitHub.

### API reference

- <a href="https://api-ref.agora.io/en/video-sdk/android/4.x/API/class_irtcengine.html#api_irtcengine_joinchannel2">joinChannel</a>

- <a href="https://api-ref.agora.io/en/video-sdk/android/4.x/API/class_irtcengine.html#api_irtcengine_enablevideo">enableVideo</a>

- <a href="https://api-ref.agora.io/en/video-sdk/android/4.x/API/class_irtcengine.html#api_irtcengine_startpreview">startPreview</a>

- <a href="https://api-ref.agora.io/en/video-sdk/android/4.x/API/class_irtcengine.html#api_irtcengine_leavechannel">leaveChannel</a>

