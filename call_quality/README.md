# Call quality best practice

Customer satisfaction for your Video Calling integrated app depends on the quality of video and audio it provides. Quality of audiovisual communication through your app is affected by the following factors:

- Bandwidth of network connection: Bandwidth is the volume of information that an Internet connection can handle per unit of time. When the available bandwidth is not sufficient to transmit the amount of data necessary to provide the desired video quality, your users see jerky or frozen video along with audio that cuts in and out.

- Stability of network connection: Network connections are often unstable with the network quality going up and down. Users get temporarily disconnected and come back online after an interruption. These issues lead to a poor audiovisual experience for your users unless your app is configured to respond to these situations and take remedial actions.

- Hardware quality: The camera and microphone used to capture video and audio must be of sufficiently good quality. If the user's hardware does not capture the audiovisual information in suitably high definition, it limits the quality of audio and video that is available to the remote user.

- Video and audio settings: The sharpness, smoothness, and overall quality of the video is directly linked to the frame rate, bitrate and other video settings. Similarly, the audio quality depends on the sample rate, bitrate, number of channels and other audio parameters. If you do not choose proper settings, the audio and video transmitted are of poor quality. On the other hand, if the settings are too demanding, the available bandwidth quickly gets choked, leading to suboptimal experience for your users.

- Echo: Echo is produced when your audio signal is played by a remote user through a speakerphone or an external device. This audio is captured by the remote user's microphone and sent back to you. Echo negatively affects audio quality, making speech difficult to understand.

- Multiple users in a channel: When multiple users engage in real-time audio and video communication in a channel, the available bandwidth is quickly used up due to several incoming audio and video streams. The device performance also deteriorates due to the excessive workload required to decode and render multiple video streams.

- Latency: Latency is the time it takes for a single video frame to transfer from the sender's camera to the receiver's display. Network routers are the most common cause of latency on the end-to-end path. Satellite communication also adds significant latency to audio and video streaming.

This sample project shows you how to use Video SDK features to account for these factors and ensure optimal audio and video quality in your app.

## Understand the tech

Video SDK provides the following features to deal with channel quality issues:

* **Network probe test**: The network probe test checks the last-mile network quality before you join a channel. The method returns network quality statistics including round-trip latency, packet loss rate, and network bandwidth.

* **Echo test**: The echo test captures audio through the microphone on the user’s device, and sends it to <Vg k = "AGORA_BACKEND"/>. After a delay of about 2 seconds, <Vg k = "AGORA_BACKEND"/> sends the audio back to the sending device to be played. The returned audio enable a user to judge if their hardware and network connection are of adequate quality. Agora recommends that an echo test be performed before a network probe test.

* **Audio profiles**: Delivering the best quality audio to your users requires choosing audio settings customized for your particular application. In Video SDK you can choose from pre-configured audio profiles and audio scenarios to optimize audio settings for a wide range of applications.

    * An _audio profile_ sets the audio sample rate, bitrate, encoding scheme, and the number of channels for your audio. Video SDK offers several preset audio profiles to choose from. To pick the most suitable audio profile for your application, refer to the [List of audio profiles](#list-of-audio-profiles).
    
    * An _audio scenario_ specifies the audio performance in terms of volume, audio quality, and echo cancellation. Based on the nature of your application, you can pick the most suitable option from the [List of audio scenarios](#list-of-audio-scenarios). 

* **Video profiles**: In real-time engagement scenarios, user experience is closely tied to the sharpness, smoothness, and overall quality of the video. In Video SDK you can set the video dimensions, framerate, bitrate, orientation mode, and mirror mode by specifying a video profile. You can also set the degradation preference to specify how video quality is degraded under suboptimal network conditions. To find the suitable bitrate for a given combination of dimensions and framerate, refer to the [Video profile table](#video-profile-table).

* **In-call quality statistics**: Video SDK provides several callbacks and methods to monitor channel quality in real-time. These methods and callbacks provide vital statistics to evaluate communication quality and provide the information necessary to take remedial actions. Video SDK provides you the following statistics :

    * _Network quality_: The uplink and downlink network quality in terms of the transmission bitrate, packet loss rate, average Round-Trip Time, and jitter in your network.

    * _Call quality_: Information on the current user session and the resources being used by the channel in terms of the number of users in a channel, packet loss rate, CPU usage and call duration. Use these statistics to troubleshoot call quality issues. 

    * _Local audio quality_: Local audio measurements such as audio channels, sample rate, sending bitrate, and packet loss rate in the audio stream. 

    * _Remote audio quality_: These statistics provide information such as the number of channels, received bitrate, jitter in the audio stream, audio loss rate, and packet loss rate.

    * _Local video quality_: Local video quality statistics such as packet loss rate, frame rate, encoded frame width, and sent bitrate. 

    * _Remote video quality_: These statistics include information about the width and height of video frames, packet loss rate, receiving stream type, and bitrate in the reported interval. 

    * _Video and Audio states_: <Vg k="AGORA_BACKEND"/> reports the new state, and the reason for state change, whenever the state of an audio or video stream changes.

* **Dual stream mode**: In dual-stream mode, Video SDK transmits a high-quality and a low-quality video stream from the sender. The high-quality stream has higher resolution and bitrate than the the low-quality video stream. Remote users subscribe to the low-quality stream to improve communication continuity as it reduces bandwidth consumption. Subscribers should also choose the low-quality video streams when network condition are unreliable, or when multiple users publish streams in a channel.

* **Video stream fallback**: When network conditions deteriorate, Video SDK automatically switches the video stream from high-quality to low-quality, or disables video to ensure audio delivery. <Vg k="AGORA_BACKEND"/> continues to monitor the network quality after fallback, and restores the video stream when network conditions allow it. To improve communication quality under extremely poor network conditions, implement a fallback option in your app.

* **Video for multiple users**: When multiple users join a channel, several incoming high-quality video streams negatively impact network and device performance. In such cases, you can manage the excess load by playing high-quality video from the user who has focus, and low quality streams from all other users. To implement this feature, it is necessary for all users in the channel to enable the dual stream mode.

* **Echo cancellation when playing audio files**: Video SDK offers audio mixing functionality to play media in a channel. You can mix a local or online audio file with the audio captured through the microphone, or completely replace the microphone audio. Audio mixing takes advantage of the echo cancellation features of Video SDK to reduce echo in a channel. Refer to <Link to="audio-and-voice-effects">Audio and voice effects</Link> to learn more about audio mixing in Video SDK.

* **Connection state monitoring**: The connection state between an app and <Vg k="AGORA_BACKEND"/> changes when the app joins or leaves a channel, or goes offline due to network or authentication issues. Video SDK provides connection state monitoring to detect when and why a network connection is interrupted. When the connection state changes, <Vg k="AGORA_BACKEND"/> sends a callback to notify the app. Video SDK then automatically tries to reconnect to the server to restore the connection.

- **Log files**: Video SDK provides configuration options that you use to customize the location, content and size of log files containing key data of Video SDK operation. When you set up logging, Video SDK writes information messages, warnings, and errors regarding activities such as initialization, configuration, connection and disconnection to log files. Log files are useful in detecting and resolving channel quality issues.

The following figure shows the workflow you need to implement to ensure channel quality in your app:

![Ensure Channel Quality](./images/ensure-channel-quality.png)

## Reference

To view essential code snippets and their explanation, refer to the complete project documentation for your product of interest:

* [Video-calling](https://docs.agora.io/en/video-calling/develop/ensure-channel-quality?platform=android)
* [Voice-Calling](https://docs.agora.io/en/voice-calling/develop/ensure-channel-quality?platform=android)
* [Interactive live Streaming](https://docs.agora.io/en/interactive-live-streaming/develop/ensure-channel-quality?platform=android)
* [Broadcast streaming](https://docs.agora.io/en/broadcast-streaming/develop/ensure-channel-quality?platform=android)

