package io.agora.product_workflow_manager

import android.content.Context
import android.view.SurfaceView
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.ScreenCaptureParameters

class ProductWorkflowManager(context: Context?) : AuthenticationManager(context) {

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