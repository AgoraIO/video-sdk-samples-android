package io.agora.product_workflow_manager

import android.content.Context
import android.util.DisplayMetrics
import android.view.SurfaceView
import androidx.activity.result.contract.ActivityResultContracts

import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.ScreenCaptureParameters
import io.agora.rtc2.video.VideoCanvas


class ProductWorkflowManager(context: Context?) : AuthenticationManager(context) {

    enum class VolumeTypes(val description: String, val intValue: Int) {
        PLAYBACK_SIGNAL_VOLUME("Playback signal volume", 1),
        RECORDING_SIGNAL_VOLUME("Recording signal volume", 2),
        USER_PLAYBACK_SIGNAL_VOLUME("User playback signal volume", 3),
        AUDIO_MIXING_VOLUME("Audio mixing volume", 4),
        AUDIO_MIXING_PLAYOUT_VOLUME("Audio mixing play-out volume", 5),
        AUDIO_MIXING_PUBLISH_VOLUME("Audio mixing publish volume", 6),
        CUSTOM_AUDIO_PLAYOUT_VOLUME("Custom audio play-out volume", 7),
        CUSTOM_AUDIO_PUBLISH_VOLUME("Custom audio publish volume", 8),
    }

    fun adjustVolume(volumeParameter: VolumeTypes, volume: Int) {
        when (volumeParameter) {
            VolumeTypes.PLAYBACK_SIGNAL_VOLUME -> {
                agoraEngine?.adjustPlaybackSignalVolume(volume)
            }
            VolumeTypes.RECORDING_SIGNAL_VOLUME -> {
                agoraEngine?.adjustRecordingSignalVolume(volume)
            }
            VolumeTypes.USER_PLAYBACK_SIGNAL_VOLUME -> {
                if (remoteUids.size > 0) {
                    val remoteUid = remoteUids.first() // the uid of the remote user
                    agoraEngine?.adjustUserPlaybackSignalVolume(remoteUid, volume)
                }
            }
            VolumeTypes.AUDIO_MIXING_VOLUME -> {
                agoraEngine?.adjustAudioMixingVolume(volume)
            }
            VolumeTypes.AUDIO_MIXING_PLAYOUT_VOLUME -> {
                agoraEngine?.adjustAudioMixingPlayoutVolume(volume)
            }
            VolumeTypes.AUDIO_MIXING_PUBLISH_VOLUME -> {
                agoraEngine?.adjustAudioMixingPublishVolume(volume)
            }
            VolumeTypes.CUSTOM_AUDIO_PLAYOUT_VOLUME -> {
                agoraEngine?.adjustAudioMixingPlayoutVolume(volume)
            }
            VolumeTypes.CUSTOM_AUDIO_PUBLISH_VOLUME -> {
                val trackId = 0 // use the id of your custom audio track
                agoraEngine?.adjustCustomAudioPublishVolume(trackId, volume)
            }
        }
    }

    fun mute(muted: Boolean) {
        // Stop or resume publishing the local video stream
        agoraEngine?.muteLocalAudioStream(muted)
        // Stop or resume subscribing to the video streams of all remote users
        agoraEngine?.muteAllRemoteAudioStreams(muted)
        // Stop or resume subscribing to the audio stream of a specified user
        // agoraEngine?.muteRemoteAudioStream(remoteUid, muted)
    }

    fun startScreenSharing(metrics: DisplayMetrics) {
        // Set screen capture parameters
        val screenCaptureParameters = ScreenCaptureParameters()
        screenCaptureParameters.captureVideo = true
        screenCaptureParameters.captureAudio = true
        screenCaptureParameters.videoCaptureParameters.width = metrics.widthPixels
        screenCaptureParameters.videoCaptureParameters.height = metrics.heightPixels
        screenCaptureParameters.videoCaptureParameters.framerate = 15
        screenCaptureParameters.audioCaptureParameters.captureSignalVolume = 100

        // Start screen sharing
        agoraEngine!!.startScreenCapture(screenCaptureParameters)
        // Update channel media options to publish the screen sharing video stream
        updateMediaPublishOptions(true)
    }

    fun stopScreenSharing() {
        agoraEngine!!.stopScreenCapture()
        // Restore camera and microphone publishing
        updateMediaPublishOptions(false)
    }

    fun screenShareSurfaceView(): SurfaceView {
        // Create render view by RtcEngine
        val surfaceView = SurfaceView(mContext)
        // Setup and return a SurfaceView to render your screen sharing preview
        agoraEngine?.setupLocalVideo(VideoCanvas(surfaceView, Constants.RENDER_MODE_FIT, 0))
        agoraEngine?.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY)
        return surfaceView
    }

    private fun updateMediaPublishOptions(publishScreen: Boolean) {
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.publishCameraTrack = !publishScreen
        mediaOptions.publishMicrophoneTrack = !publishScreen
        mediaOptions.publishScreenCaptureVideo = publishScreen
        mediaOptions.publishScreenCaptureAudio = publishScreen
        agoraEngine!!.updateChannelMediaOptions(mediaOptions)
    }

}