# SDK quickstart

Agora’s Video SDK makes it easy to embed real-time video or voice chat into web, mobile and native apps. It enables one-to-one or group video/voice chat connections with smooth, jitter-free streaming. Thanks to Agora’s intelligent and global Software Defined Real-time Network (Agora SD-RTN™), you can rely on the highest available video and audio quality.

This example shows the minimum code you need to integrate high-quality, low-latency communication features into your app using Agora Video SDK.

## Understand the code

Most of the business logic for the Agora quickstart guide can be found in the `AgoraManager` class. The `AgoraManager` class encapsulates the basic logic to set up Agora Video SDK, join, and leave a channel. All projects in this repository extend this class to functionality specific for that project.

To view the UI implementation, refer to the [`BasicImplementationActivity.kt`](android-reference-app/app/src/main/java/io/agora/android_reference_app/BasicImplementationActivity.kt).

## The `config.json` file

This section provides information about the configuration file [`config.json`](./src/main/res/raw/config.json) used in the project. The `AgoraManager` class loads the configuration data from this file. The file has the following structure:

```json
{
    "uid": "0",
    "appId": "<--Your app Id-->",
    "channelName": "demo",
    "rtcToken": "<--Authentication token from Agora Console-->",
    "serverUrl": "<--Token server url-->",
    "tokenExpiryTime": "600"
}
```

### Properties

- `uid`: The user ID associated with the application.
- `appId`: The unique ID for the application obtained from https://console.agora.io.
- `channelName`: The name of the channel to join.
- `rtcToken`: The RTC (Real-Time Communication) token generated for authentication.
- `serverUrl`: The URL for the token generator.
- `tokenExpiryTime`: The time in seconds after which a token expires.

Ensure that the [`config.json`](./src/main/res/raw/config.json) file is correctly populated with the required values before running the application.


For context on this implementation, and a full explanation of the essential code snippets used in this example, read the **SDK quickstart** document for your product of interest:

* [Video Calling](https://docs.agora.io/en/video-calling/get-started/get-started-sdk?platform=android)
* [Voice Calling](https://docs.agora.io/en/voice-calling/get-started/get-started-sdk?platform=android)
* [Interactive Live Streaming](https://docs.agora.io/en/video-calling/get-started/get-started-sdk?platform=android)
* [Broadcast Streaming](https://docs.agora.io/en/video-calling/get-started/get-started-sdk?platform=android)

## How to run this project

To see how to run this project, refer to [README](../README.md) in the root folder or one of the complete product guides.
