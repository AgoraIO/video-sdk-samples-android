package io.agora.raw_video_audio_manager

import android.content.Context
import io.agora.authentication_manager.AuthenticationManager
import io.agora.base.VideoFrame
import io.agora.rtc2.Constants
import io.agora.rtc2.IAudioFrameObserver
import io.agora.rtc2.audio.AudioParams
import io.agora.rtc2.video.IVideoFrameObserver
import java.nio.ByteBuffer


class RawVideoAudioManager(context: Context?) : AuthenticationManager(context) {
    private var isZoomed = false
    // Set the format of the captured raw audio data.
    private val sampleRate = 16000
    private val numberOfChannels = 1
    private val samplesPerCall = 1024

    private val iAudioFrameObserver: IAudioFrameObserver = object : IAudioFrameObserver {
        override fun onRecordAudioFrame(
            channelId: String?,
            type: Int,
            samplesPerChannel: Int,
            bytesPerSample: Int,
            channels: Int,
            samplesPerSec: Int,
            buffer: ByteBuffer?,
            renderTimeMs: Long,
            avsync_type: Int
        ): Boolean {
            // Gets the captured audio frame.
            // Add code here to process the recorded audio.
            return false
        }

        override fun onPlaybackAudioFrame(
            channelId: String?,
            type: Int,
            samplesPerChannel: Int,
            bytesPerSample: Int,
            channels: Int,
            samplesPerSec: Int,
            buffer: ByteBuffer?,
            renderTimeMs: Long,
            avsync_type: Int
        ): Boolean {
            // Gets the audio frame for playback.
            // Add code here to process the playback audio.
            // return true to indicate that Data has been processed
            return false
        }

        override fun onMixedAudioFrame(
            channelId: String?,
            type: Int,
            samplesPerChannel: Int,
            bytesPerSample: Int,
            channels: Int,
            samplesPerSec: Int,
            buffer: ByteBuffer?,
            renderTimeMs: Long,
            avsync_type: Int
        ): Boolean {
            // Retrieves the mixed captured and playback audio frame.
            return false
        }

        override fun onEarMonitoringAudioFrame(
            type: Int,
            samplesPerChannel: Int,
            bytesPerSample: Int,
            channels: Int,
            samplesPerSec: Int,
            buffer: ByteBuffer?,
            renderTimeMs: Long,
            avsync_type: Int
        ): Boolean {
            return false
        }

        override fun onPlaybackAudioFrameBeforeMixing(
            channelId: String?,
            userId: Int,
            type: Int,
            samplesPerChannel: Int,
            bytesPerSample: Int,
            channels: Int,
            samplesPerSec: Int,
            buffer: ByteBuffer?,
            renderTimeMs: Long,
            avsync_type: Int
        ): Boolean {
            // Retrieves the audio frame of a specified user before mixing.
            return false
        }

        override fun getObservedAudioFramePosition(): Int {
            return 0
        }

        override fun getRecordAudioParams(): AudioParams {
            return AudioParams(sampleRate,numberOfChannels, 0 ,samplesPerCall)
        }

        override fun getPlaybackAudioParams(): AudioParams {
            return AudioParams(sampleRate,numberOfChannels, 0 ,samplesPerCall)
        }

        override fun getMixedAudioParams(): AudioParams {
            return AudioParams(sampleRate,numberOfChannels, 0 ,samplesPerCall)
        }

        override fun getEarMonitoringAudioParams(): AudioParams {
            return AudioParams(sampleRate,numberOfChannels, 0 ,samplesPerCall)
        }
    }

    private val iVideoFrameObserver: IVideoFrameObserver = object : IVideoFrameObserver {
        override fun onCaptureVideoFrame(sourceType: Int, videoFrame: VideoFrame): Boolean {
            if (isZoomed) {
                // Read the videoFrame buffer
                var buffer = videoFrame.buffer

                val w = buffer.width
                val h = buffer.height
                val cropX = (w - 320) / 2
                val cropY = (h - 240) / 2
                val cropWidth = 320
                val cropHeight = 240
                val scaleWidth = 320
                val scaleHeight = 240

                // modify the buffer
                buffer = buffer.cropAndScale(
                    cropX, cropY,
                    cropWidth, cropHeight,
                    scaleWidth, scaleHeight
                )

                // replace the videoFrame buffer with the modified buffer
                videoFrame.replaceBuffer(buffer, 270, videoFrame.timestampNs)
            }
            return true
        }

        override fun onPreEncodeVideoFrame(sourceType: Int, videoFrame: VideoFrame?): Boolean {
            return false
        }

        override fun onMediaPlayerVideoFrame(videoFrame: VideoFrame, i: Int): Boolean {
            return false
        }

        override fun onRenderVideoFrame(s: String, i: Int, videoFrame: VideoFrame): Boolean {
            return true
        }

        override fun getVideoFrameProcessMode(): Int {
            // The process mode of the video frame. 0 means read-only, and 1 means read-and-write.
            return 1
        }

        override fun getVideoFormatPreference(): Int {
            return 1
        }

        override fun getRotationApplied(): Boolean {
            return false
        }

        override fun getMirrorApplied(): Boolean {
            return false
        }

        override fun getObservedFramePosition(): Int {
            return 0
        }
    }

    override fun joinChannel(channelName: String, token: String?): Int {
        // Register the video frame observer
        agoraEngine!!.registerVideoFrameObserver(iVideoFrameObserver)
        // Register the audio frame observer
        agoraEngine!!.registerAudioFrameObserver(iAudioFrameObserver)

        agoraEngine!!.setRecordingAudioFrameParameters(
            sampleRate, numberOfChannels,
            Constants.RAW_AUDIO_FRAME_OP_MODE_READ_WRITE, samplesPerCall
        )
        agoraEngine!!.setPlaybackAudioFrameParameters(
            sampleRate, numberOfChannels,
            Constants.RAW_AUDIO_FRAME_OP_MODE_READ_WRITE, samplesPerCall
        )
        agoraEngine!!.setMixedAudioFrameParameters(
            sampleRate,
            numberOfChannels,
            samplesPerCall
        )

        return super.joinChannel(channelName, token)
    }

    override fun leaveChannel() {
        agoraEngine!!.registerVideoFrameObserver(null)
        agoraEngine!!.registerAudioFrameObserver(null)

        super.leaveChannel()
    }

    fun setZoom(enable: Boolean) {
        isZoomed = enable
    }

}