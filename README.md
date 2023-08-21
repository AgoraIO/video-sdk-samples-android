# Video SDK reference app (Android)

This app demonstrates use of Agora's Video SDK for real-time audio and video communication. It is a robust and comprehensive documentation reference app for Android, designed to enhance your productivity and understanding. It's built to be flexible, easily extensible, and beginner-friendly.

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

    Launch Android Studio. From the **File** menu, select **Open...** and navigate to the [android-reference-app](android-reference-app) folder. Start Gradle sync to automatically install all project dependencies.

1. Modify `config.json`

   The app loads connection parameters from the [`config.json`](./agora-manager/src/main/res/raw/config.json) file. Ensure that the file is populated with the required parameter values before running the application.

    - `uid`: The user ID associated with the application.
    - `appId`: (Required) The unique ID for the application obtained from [Agora Console](https://console.agora.io). 
    - `channelName`: The default name of the channel to join.
    - `rtcToken`: An RTC (Real-Time Communication) token generated for the `channelName`. You can generate a temporary token using Agora Console.
    - `serverUrl`: The URL for the token generator. See [Secure authentication with tokens](authentication-workflow) fpr information on how to set up a token server.
    - `tokenExpiryTime`: The time in seconds after which a token expires.

    If a valid `serverUrl` is provided, all examples use the token server to obtain a token except the **SDK quickstart** project that uses the `rtcToken`. If a `serverUrl` is not specified, all examples except **Secure authentication with tokens** use the `rtcToken` from `config.json`.

1. Build and run the project

    To build and run the project, select your connected Android device or emulator and press the **Run** button in Android Studio.

1. From the main app screen, choose and launch an example.

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


https://github.com/AgoraIO/video-sdk-samples-android/assets/65331551/65de543d-2977-46f9-92b5-37a05413845b


## Contact

If you have any questions, issues, or suggestions, please file an issue in our [GitHub Issue Tracker](https://github.com/AgoraIO/video-sdk-samples-android/issues).
