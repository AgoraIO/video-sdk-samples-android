# Secure authentication with tokens

Authentication is the act of validating the identity of each user before they access a system. Agora uses digital tokens to authenticate users and their privileges before they access Agora SD-RTN™ to join Video Calling. Each token is valid for a limited period and works only for a specific channel. For example, you cannot use the token generated for a channel called *AgoraChannel* to join the *AppTest* channel.

This example shows you how to retrieve a token from an authentication server, and use it to connect securely to a specific Video SDK channel. 

### Deploy a token server

To quickly deploy a token server, use one of the [one-click deployment](https://github.com/AgoraIO-Community/agora-token-service#one-click-deployments) methods or implement your own server using the code [here](https://github.com/AgoraIO-Community/agora-token-service). You use your token server URL for user authentication in this example.

## Understand the code

For context on this implementation, and a full explanation of the essential code snippets used in this example, read the **Secure authentication with tokens** document for your product of interest:

* [Video Calling](https://docs.agora.io/en/video-calling/get-started/authentication-workflow?platform=android)
* [Voice Calling](https://docs.agora.io/en/voice-calling/get-started/authentication-workflow?platform=android)
* [Interactive Live Streaming](https://docs.agora.io/en/video-calling/get-started/authentication-workflow?platform=android)
* [Broadcast Streaming](https://docs.agora.io/en/video-calling/get-started/authentication-workflow?platform=android)

## How to run this project

To see how to run this project, refer to [README](../README.md) in the root folder or one of the complete product guides.
