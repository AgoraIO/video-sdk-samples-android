# SDK quickstart

Agora Video SDK makes it easy to embed real-time video or voice chat into web, mobile and native apps. It enables one-to-one or group video and voice chat connections with smooth, jitter-free streaming. Thanks to Agora’s intelligent and global Software Defined Real-time Network (Agora SD-RTN™), you can rely on the highest available video and audio quality.

This example shows the minimum code you need to integrate high-quality, low-latency communication audio and video features into your Android app using Agora Video SDK.

## Understand the code

You find the business logic for this quickstart sample in the [`AgoraManager`](src/main/java/io/agora/agora_manager/AgoraManager.kt) class. This class encapsulates the code to set up an instance of `RTCEngine`, join, and leave a channel. All examples in this repository extend the `AgoraManager` class to add functionality specific for that application.

For context on this implementation, and a full explanation of the essential code snippets used in this example, read the **SDK quickstart** document for your product of interest:

* [Video Calling](https://docs.agora.io/en/video-calling/get-started/get-started-sdk?platform=android)
* [Voice Calling](https://docs.agora.io/en/voice-calling/get-started/get-started-sdk?platform=android)
* [Interactive Live Streaming](https://docs.agora.io/en/interactive-live-streaming/get-started/get-started-sdk?platform=android)
* [Broadcast Streaming](https://docs.agora.io/en/broadcast-streaming/get-started/get-started-sdk?platform=android)

For the UI implementation of this example, refer to [`BasicImplementationActivity.kt`](../android-reference-app/app/src/main/java/io/agora/android_reference_app/BasicImplementationActivity.kt).


## How to run this example

To see how to run this example, refer to the [README](../README.md) in the root folder or one of the complete product guides.
