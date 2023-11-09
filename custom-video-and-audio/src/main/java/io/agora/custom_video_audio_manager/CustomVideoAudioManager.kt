package io.agora.custom_video_audio_manager

import android.content.Context
import android.os.Process
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.audio.AudioTrackConfig
import java.io.IOException
import java.io.InputStream

class CustomVideoAudioManager(context: Context?) : AuthenticationManager(context) {
    // Audio file parameters
    private var customAudioTrackId = -1
    private val audioFile = "applause.wav" // raw audio file

    private val sampleRate = 44100
    private val numberOfChannels = 2
    private val bitsPerSample = 16
    private val samples = 441
    private val bufferSize = samples * bitsPerSample / 8 * numberOfChannels
    private val pushInterval = samples * 1000 / sampleRate

    private var inputStream: InputStream? = null
    private var pushingTask: Thread = Thread(PushingTask(this))

    var pushingAudio = false

    override fun joinChannel(channelName: String, token: String?): Int {
        // Ensure that necessary Android permissions have been granted
        if (!checkSelfPermission()) {
            sendMessage("Permissions were not granted")
            return -1
        }
        this.channelName = channelName

        // Create an RTCEngine instance
        if (agoraEngine == null) setupAgoraEngine()
        val options = ChannelMediaOptions()
        if (currentProduct == ProductName.VIDEO_CALLING || currentProduct == ProductName.VOICE_CALLING) {
            // For a Video/Voice call, set the channel profile as COMMUNICATION.
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            isBroadcaster = true
        } else {
            // For Live Streaming and Broadcast streaming,
            // set the channel profile as LIVE_BROADCASTING.
            options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            if (!isBroadcaster && currentProduct == ProductName.BROADCAST_STREAMING) {
                // Set Low latency for Broadcast streaming
                options.audienceLatencyLevel =
                    Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
            } else if (!isBroadcaster && currentProduct == ProductName.INTERACTIVE_LIVE_STREAMING) {
                options.audienceLatencyLevel =
                    Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY
            }
        }

        // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
        if (isBroadcaster) { // Broadcasting Host or Video-calling client
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

            // Set up custom audio
            val audioTrackConfig = AudioTrackConfig()
            audioTrackConfig.enableLocalPlayback = true

            customAudioTrackId = agoraEngine!!.createCustomAudioTrack(
                Constants.AudioTrackType.AUDIO_TRACK_MIXABLE,
                audioTrackConfig
            )
            options.publishCustomAudioTrack = true // Enable publishing custom audio
            options.publishCustomAudioTrackId = customAudioTrackId
            options.publishMicrophoneTrack = false // Disable publishing microphone audio

            // Start local preview.
            agoraEngine!!.startPreview()
        } else { // Audience
            options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
        }

        // Join the channel with a token.
        agoraEngine!!.joinChannel(token, channelName, localUid, options)
        return 0
    }

    override fun setupAgoraEngine(): Boolean {
        val result = super.setupAgoraEngine()

        // open the audio file
        try {
            inputStream = mContext.resources.assets.open(audioFile)
            // Use the inputStream as needed
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }

    internal class PushingTask(private val manager: CustomVideoAudioManager) : Runnable {
        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            while (manager.pushingAudio) {
                val before = System.currentTimeMillis()
                manager.agoraEngine?.pushExternalAudioFrame(manager.readBuffer(),
                    System.currentTimeMillis(),
                    manager.sampleRate,
                    manager.numberOfChannels,
                    Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE,
                    manager.customAudioTrackId
                )
                val now = System.currentTimeMillis()
                val consuming = now - before
                if (consuming < manager.pushInterval) {
                    try {
                        Thread.sleep(manager.pushInterval - consuming)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun handleOnJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        if (isBroadcaster) {
            // Start the pushing task
            pushingAudio = true
            pushingTask.start()
        }
    }

    private fun readBuffer(): ByteArray? {
        // Read the audio file buffer
        val byteSize = bufferSize
        val buffer = ByteArray(byteSize)
        try {
            if (inputStream!!.read(buffer) < 0) {
                inputStream!!.reset()
                return readBuffer()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return buffer
    }
}