package io.agora.geofencing_manager

import android.content.Context
import android.view.SurfaceView
import io.agora.authentication_manager.AuthenticationManager
import io.agora.mediaplayer.Constants.*
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.video.VideoCanvas

class GeofencingManager(context: Context?) : AuthenticationManager(context) {
    private var mediaPlayer: IMediaPlayer? = null // Instance of the media player
    private var mediaDuration: Long = 0
    private lateinit var mediaPlayerListener: MediaPlayerListener

    // Interface to forward media player events to the UI
    interface MediaPlayerListener {
        fun onPlayerStateChanged(state: MediaPlayerState, error: MediaPlayerError)
        fun onProgress(percent: Int)
    }

    fun setupMediaPlayer(listener: MediaPlayerListener){
        if (mediaPlayer == null) {
            // Create an instance of the media player
            mediaPlayer = agoraEngine?.createMediaPlayer()
            // Set the mediaPlayerObserver to receive callbacks
            mediaPlayer?.registerPlayerObserver(mediaPlayerObserver)
            // A listener to notify the UI
            mediaPlayerListener = listener
        }
    }

    fun mediaPlayerState(): MediaPlayerState? {
        // Returns the current state of the media player
        return mediaPlayer?.state
    }

    fun openMediaFile(mediaLocation: String) {
        // Opens the media file at mediaLocation url
        // Supports URI files starting with content://
        if (mediaPlayer != null) {
            // Open the media file
            mediaPlayer?.open(mediaLocation, 0)
        }
    }

    fun destroyMediaPlayer(){
        // Destroy the media player instance and clean up
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.unRegisterPlayerObserver(mediaPlayerObserver)
            mediaPlayer?.destroy()
            mediaPlayer = null
        }
    }

    fun playMedia() {
        // Start publishing the media player video
        updateChannelPublishOptions(true)
        mediaPlayer?.play()
    }

    fun pauseMedia() {
        mediaPlayer?.pause()
    }

    fun resumeMedia() {
        mediaPlayer?.resume()
    }

    fun updateChannelPublishOptions(publishMediaPlayer: Boolean) {
        val channelOptions = ChannelMediaOptions()
        // Start or stop publishing the media player tracks
        channelOptions.publishMediaPlayerAudioTrack = publishMediaPlayer
        channelOptions.publishMediaPlayerVideoTrack = publishMediaPlayer
        // Stop or start publishing the microphone and camera tracks
        channelOptions.publishMicrophoneTrack = !publishMediaPlayer
        channelOptions.publishCameraTrack = !publishMediaPlayer
        // Specify the media player Id for publishing
        if (publishMediaPlayer) channelOptions.publishMediaPlayerId = mediaPlayer?.mediaPlayerId
        // Implement the settings
        agoraEngine?.updateChannelMediaOptions(channelOptions)
    }

    fun mediaPlayerSurfaceView(): SurfaceView {
        // Sets up and returns a SurfaceView to display the media player output
        // Instantiate a SurfaceView
        val videoSurfaceView = SurfaceView(mContext)
        // Create a VideoCanvas using the SurfaceView
        val videoCanvas =  VideoCanvas(
            videoSurfaceView,
            Constants.RENDER_MODE_HIDDEN,
            0
        )
        // Set the source type and media player Id
        videoCanvas.sourceType = Constants.VIDEO_SOURCE_MEDIA_PLAYER
        videoCanvas.mediaPlayerId = mediaPlayer?.mediaPlayerId ?: 0
        // Setup the SurfaceView
        agoraEngine?.setupLocalVideo(videoCanvas)

        return videoSurfaceView
    }

    private val mediaPlayerObserver: IMediaPlayerObserver = object : IMediaPlayerObserver {
        override fun onPlayerStateChanged(state: MediaPlayerState, error: MediaPlayerError) {
            // Reports changes in playback state
            if (state == MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                // Read media duration for updating play progress
                mediaDuration = mediaPlayer?.duration ?: 0
            }

            // Notify the UI
            mediaPlayerListener.onPlayerStateChanged(state, error)
        }

        override fun onPositionChanged(position: Long) {
            if (mediaDuration > 0) {
                // Calculate the progress percentage
                val result = (position.toFloat() / mediaDuration.toFloat() * 100).toInt()
                // Notify the UI of the progress
                mediaPlayerListener.onProgress(result)
            }
        }

        override fun onPlayerEvent(eventCode: MediaPlayerEvent, elapsedTime: Long, message: String) {
            // Required to implement IMediaPlayerObserver
        }

        override fun onMetaData(type: MediaPlayerMetadataType, data: ByteArray) {
            // Occurs when the media metadata is received
        }

        override fun onPlayBufferUpdated(playCachedBuffer: Long) {
            // Reports the playback duration that the buffered data can support
        }

        override fun onPreloadEvent(src: String, event: MediaPlayerPreloadEvent) {
            // Reports the events of preloaded media resources
        }

        override fun onPlayerSrcInfoChanged(from: SrcInfo, to: SrcInfo) {
            // Occurs when the video bitrate of the media resource changes
        }

        override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo) {
            // Occurs when information related to the media player changes
        }

        override fun onAudioVolumeIndication(volume: Int) {
            // Reports the volume of the media player
        }

        override fun onAgoraCDNTokenWillExpire() {
            // Required to implement IMediaPlayerObserver
        }
    }

}