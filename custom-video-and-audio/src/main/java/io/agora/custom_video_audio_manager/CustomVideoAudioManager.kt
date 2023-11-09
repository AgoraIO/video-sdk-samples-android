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
    // Configure audio parameters
    private val sampleRate = 44100
    private val numberOfChannels = 2
    private val bitsPerSample = 16
    private val samples = 441
    private val bufferSize = samples * bitsPerSample / 8 * numberOfChannels
    private val pushInterval = samples * 1000 / sampleRate

    private var inputStream: InputStream? = null
    private var pushingTask: Thread? = null

    var pushingAudio = false

    fun playCustomAudio() {
        // Create a custom audio track
        val audioTrackConfig = AudioTrackConfig()
        audioTrackConfig.enableLocalPlayback = true

        customAudioTrackId = agoraEngine!!.createCustomAudioTrack(
            Constants.AudioTrackType.AUDIO_TRACK_MIXABLE,
            audioTrackConfig
        )

        // Set custom audio publishing options
        val options = ChannelMediaOptions()
        options.publishCustomAudioTrack = true // Enable publishing custom audio
        options.publishCustomAudioTrackId = customAudioTrackId
        options.publishMicrophoneTrack = false // Disable publishing microphone audio
        agoraEngine!!.updateChannelMediaOptions(options)

        // Open the audio file
        openAudioFile()

        // Start the pushing task
        pushingTask = Thread(PushingTask(this))
        pushingAudio = true
        pushingTask?.start()
    }

    fun openAudioFile() {
        // Open the audio file
        try {
            inputStream = mContext.resources.assets.open(audioFile)
            // Use the inputStream as needed
        } catch (e: IOException) {
            e.printStackTrace()
        }
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

    fun stopCustomAudio() {
        pushingAudio = false
        pushingTask?.interrupt()
    }
}