# Video SDK reference app (Android)

This app demonstrates using Agora's Video SDK for real-time communication in an Android app.

This example app is a robust and comprehensive documentation reference app for Android, designed to enhance your productivity and understanding. It's built to be flexible, easily extensible, and beginner-friendly.

To understand the contents better, you can go to [Agora's Documentation](https://docs.agora.io), which explains each example in more detail.

## Prerequisites

Before getting started with this example app, please ensure you have the following set up:

- Android Studio 4.1 or higher
- Android SDK API Level 24 or higher
- A mobile device that runs Android 4.1 or higher
- An Agora account and project
- A computer with Internet access. Ensure that no firewall is blocking your network communication.

## Run the App

1. Clone the repository

    To clone the repository to your local machine, open Terminal and navigate to the directory where you want to clone the repository. Then, use the following command:

    ```sh
    git clone https://github.com/AgoraIO/video-sdk-samples-android.git
    ```

1. Open the project

    Launch Android Studio. From the File menu, select **Open...** and navigate to the [android-reference-app](android-reference-app) folder. All project dependencies are automatically installed when you perform a Gradle sync.

1. Modify `config.json`

    The file [config.json](agora-manager/res/raw/config.json) is located in the `agora-manager/res/raw/' directory. Open the file and update the following:

    - `appId`: Update the value to your Agora Project ID from [Agora Console](https://console.agora.io)

        You may also need to modify these parameters:

    - `rtcToken`: Generate a temporary token from [Agora Console](https://console.agora.io) and add it here.
    - `tokenUrl`: Add the URL to your token server if you have one; there are a few one-click deployment options available [here](https://github.com/AgoraIO-Community/agora-token-service).

1. Build and run the project

    To build and run the project, select your connected Android device or emulator and press the **Run** button in Android Studio.

## Examples

This demo app includes several examples that illustrate the functionality and features of Agora Video/Voice SDK. Each example is self-contained and the relevant code can be found in its own folder in the root directory. For more information about each example, see the README file within the directory.

- [SDK quickstart](agora-manager)
- [Secure authentication with tokens](authentication-workflow)
- [Call quality best practice](ensure-channel-quality)
- [Connect through restricted networks with Cloud Proxy](cloud-proxy)
- [Stream media to a channel](play-media)
- [Secure channel encryption](media-stream-encryption)
- [Screen share, volume control and mute](product-workflow)
- [Custom video and audio sources](custom-video-and-audio)
- [Raw video and audio processing](stream-raw-audio-and-video)
- [Geofencing](geofencing)

To view the UI implementation, open the relevant Activity Class file [here]( android-reference-app/app/src/main/java/io/agora/android_reference_app).

## Contact

If you have any questions, issues, or suggestions, please file an issue in our [GitHub Issue Tracker](https://github.com/AgoraIO/video-sdk-samples-android/issues).
