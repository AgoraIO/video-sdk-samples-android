package io.agora.custom_video_audio_manager

import android.content.Context

import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.os.Build
import android.os.Process
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import androidx.annotation.RequiresApi
import io.agora.authentication_manager.AuthenticationManager
import io.agora.base.VideoFrame
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.audio.AudioTrackConfig
import java.io.IOException
import java.io.InputStream


class CustomVideoAudioManager(context: Context?) : AuthenticationManager(context) {
    // Custom video parameters
    private lateinit var previewTextureView: TextureView
    private var previewSurfaceTexture: SurfaceTexture? = null

    private var mTextureDestroyed = false
    private var mPreviewing = false

    // Custom audio parameters
    private var customAudioTrackId = -1
    private val audioFile = "applause.wav" // raw audio file
    private val sampleRate = 44100
    private val numberOfChannels = 2
    private val bitsPerSample = 16
    private val samples = 441
    private val bufferSize = samples * bitsPerSample / 8 * numberOfChannels
    private val pushInterval = samples * 1000 / sampleRate
    private var inputStream: InputStream? = null
    private var pushingTask: Thread? = null
    var pushingAudio = false

    // Custom Video
    fun setupCustomVideo () {
        // Enable publishing of the captured video from a custom source
        val options = ChannelMediaOptions()
        options.publishCustomVideoTrack = true
        options.publishCameraTrack = false

        agoraEngine!!.updateChannelMediaOptions(options)

        // Configure the external video source.
        agoraEngine!!.setExternalVideoSource(
            true,
            true,
            Constants.ExternalVideoSourceType.VIDEO_FRAME
        )

        // Check whether texture encoding is supported
        sendMessage(if (agoraEngine!!.isTextureEncodeSupported) "Texture encoding is supported" else "Texture encoding is not supported")
    }

    private val surfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onSurfaceTextureAvailable(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            // Invoked when a TextureView's SurfaceTexture is ready for use.
            if (mPreviewing) {
                // Already previewing custom video
                return
            }
            sendMessage("Surface Texture Available")
            mTextureDestroyed = false

            // Set up previewSurfaceTexture
            previewSurfaceTexture = SurfaceTexture(true)
            previewSurfaceTexture!!.setOnFrameAvailableListener(onFrameAvailableListener)

            // Add code here to:
            // * set up and configure the custom video source
            // * set SurfaceTexture of the custom video source to previewSurfaceTexture
            sendMessage("Add your code to configure a custom video source")

            // Start preview
            mPreviewing = true
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            mTextureDestroyed = true
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private val onFrameAvailableListener = OnFrameAvailableListener {
        // Callback to notify that a new stream video frame is available.
        if (isJoined) {
            // Configure the external video frames and send them to the SDK
            val videoFrame: VideoFrame? = null

            // Add code here to convert the surfaceTexture data to a VideoFrame object

            // Send VideoFrame to the SDK
            agoraEngine!!.pushExternalVideoFrame(videoFrame)
        }
    }

    fun customLocalVideoPreview(): TextureView {
        // Create TextureView
        previewTextureView = TextureView(mContext)
        // Add a SurfaceTextureListener
        previewTextureView.surfaceTextureListener = surfaceTextureListener

        return previewTextureView
    }


    // Custom audio
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

    private fun openAudioFile() {
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