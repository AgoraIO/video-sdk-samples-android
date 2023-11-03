package io.agora.product_workflow_manager

import android.content.Context
import android.view.SurfaceView
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.ScreenCaptureParameters

class ProductWorkflowManager(context: Context?) : AuthenticationManager(context) {

    public enum class VolumeTypes(val description: String, val intValue: Int) {
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
            else -> {}
        }
    }

    fun updateChannelPublishOptions(publishMediaPlayer: Boolean) {
        val channelOptions = ChannelMediaOptions()
        // Start or stop publishing the media player tracks
        //channelOptions.publishMediaPlayerAudioTrack = publishMediaPlayer
        //channelOptions.publishMediaPlayerVideoTrack = publishMediaPlayer
        // Stop or start publishing the microphone and camera tracks
        channelOptions.publishMicrophoneTrack = !publishMediaPlayer
        channelOptions.publishCameraTrack = !publishMediaPlayer


        // Implement the settings
        agoraEngine?.updateChannelMediaOptions(channelOptions)
    }

    fun screenSharingSurfaceView(): SurfaceView {
        // Sets up and returns a SurfaceView to display the screen sharing output
        // Instantiate a SurfaceView
        val videoSurfaceView = SurfaceView(mContext)
        // Create a VideoCanvas using the SurfaceView
        val videoCanvas =  VideoCanvas(
            videoSurfaceView,
            Constants.RENDER_MODE_HIDDEN,
            0
        )
        
        // Setup the SurfaceView
        agoraEngine?.setupLocalVideo(videoCanvas)

        return videoSurfaceView
    } 
}