#  SDK quickstart

Video Calling enables one-to-one or small-group video chat connections with smooth, jitter-free streaming video. Agora’s Video SDK makes it easy to embed real-time video chat into web, mobile and native apps.

Thanks to Agora’s intelligent and global Software Defined Real-time Network (Agora SD-RTN™), you can rely on the highest available video and audio quality.

This page shows the minimum code you need to integrate high-quality, low-latency Video Calling features into your app using Video SDK.

## Topics

### Agora Logic

Most of the business logic for the Agora quickstart guide can be found in [AgoraManager](AgoraManager.swift). Here you will find code snippets for [initialising the engine](AgoraManager.swift#L23-L28), monitoring when someone [joins the channel](AgoraManager.swift#L84-L86) or [leaves the channel](AgoraManager.swift#L97-L99).

Joining and leaving the channel can be found in [GettingStartedView](GettingStartedView.swift), in the onAppear and onDisappear views.

### Creating a Canvas

Creating a canvas for local or remote users in this project and example with Agora uses `AgoraRtcVideoCanvas`, and the SwiftUI class `UIViewRepresentable`. This can be found in [AgoraVideoCanvasView.swift](AgoraVideoCanvasView.swift).

## Full Documentation

[Agora's full SDK Quickstart Guide](https://docs.agora.io/en/interactive-live-streaming/get-started/get-started-sdk?platform=ios)
