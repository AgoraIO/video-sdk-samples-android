package io.agora.play_media_manager

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

class PlayMediaManager(context: Context?) : AuthenticationManager(context) {
    private var mediaPlayer: IMediaPlayer? = null // Instance of the media player
    var isMediaPlaying = false
    private var mediaDuration: Long = 0
    private lateinit var mediaPlayerListener: MediaPlayerListener

    fun setupMediaPlayer(listener: MediaPlayerListener){
        if (mediaPlayer == null) {
            // Create an instance of the media player
            mediaPlayer = agoraEngine?.createMediaPlayer()
            // Set the mediaPlayerObserver to receive callbacks
            mediaPlayer?.registerPlayerObserver(mediaPlayerObserver)

            mediaPlayerListener = listener
            sendMessage("Opening media file...")
            return
        }
    }

    fun mediaPlayerState(): MediaPlayerState? {
        // Check the current state of the media player
        return mediaPlayer?.state
    }

    fun openMediaFile(mediaLocation: String) {
        if (mediaPlayer != null) {
            // Open the media file
            mediaPlayer?.open(mediaLocation, 0)
        }
    }

    fun destroyMediaPlayer(){
        // Destroy and clean up
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.unRegisterPlayerObserver(mediaPlayerObserver)
            mediaPlayer?.destroy()
            mediaPlayer = null
        }
    }

    fun playMedia() {
        updateChannelPublishOptions(true)
        mediaPlayer?.play()
    }

    fun pauseMedia() {
        mediaPlayer?.pause()
    }

    fun resumeMedia() {
        mediaPlayer?.resume()
    }

    private fun updateChannelPublishOptions(publishMediaPlayer: Boolean) {
        val channelOptions = ChannelMediaOptions()
        channelOptions.publishMediaPlayerAudioTrack = publishMediaPlayer
        channelOptions.publishMediaPlayerVideoTrack = publishMediaPlayer
        channelOptions.publishMicrophoneTrack = !publishMediaPlayer
        channelOptions.publishCameraTrack = !publishMediaPlayer
        if (publishMediaPlayer) channelOptions.publishMediaPlayerId = mediaPlayer?.mediaPlayerId
        agoraEngine?.updateChannelMediaOptions(channelOptions)
    }

    fun mediaPlayerSurfaceView(): SurfaceView {
        val videoSurfaceView = SurfaceView(mContext)
        // Setup the SurfaceView to display media player output
        val videoCanvas =  VideoCanvas(
            videoSurfaceView,
            Constants.RENDER_MODE_HIDDEN,
            0
        )
        videoCanvas.mediaPlayerId = mediaPlayer?.mediaPlayerId ?: 0
        videoCanvas.sourceType = Constants.VIDEO_SOURCE_MEDIA_PLAYER
        agoraEngine?.setupLocalVideo(videoCanvas)
        return videoSurfaceView
    }

    private val mediaPlayerObserver: IMediaPlayerObserver = object : IMediaPlayerObserver {
        override fun onPlayerStateChanged(state: MediaPlayerState, error: MediaPlayerError) {
            // Notify the UI
            mediaPlayerListener.onPlayerStateChanged(state, error)

            if (state == MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                // Media file opened successfully
                mediaDuration = mediaPlayer?.duration ?: 0
            } else if (state == MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED) {
                isMediaPlaying = false
                // Stop publishing media player output and restore local video publishing
                updateChannelPublishOptions(false)
            }
        }

        override fun onPositionChanged(position: Long) {
            if (mediaDuration > 0) {
                val result = (position.toFloat() / mediaDuration.toFloat() * 100).toInt()
                mediaPlayerListener.onProgress(result)
            }
        }

        override fun onPlayerEvent(
            eventCode: MediaPlayerEvent,
            elapsedTime: Long,
            message: String
        ) {
            // Required to implement IMediaPlayerObserver
        }

        override fun onMetaData(type: MediaPlayerMetadataType, data: ByteArray) {
            // Required to implement IMediaPlayerObserver
        }

        override fun onPlayBufferUpdated(playCachedBuffer: Long) {
            // Required to implement IMediaPlayerObserver
        }

        override fun onPreloadEvent(src: String, event: MediaPlayerPreloadEvent) {
            // Required to implement IMediaPlayerObserver
        }

        override fun onAgoraCDNTokenWillExpire() {
            // Required to implement IMediaPlayerObserver
        }

        override fun onPlayerSrcInfoChanged(from: SrcInfo, to: SrcInfo) {
            // Required to implement IMediaPlayerObserver
        }

        override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo) {
            // Required to implement IMediaPlayerObserver
        }

        override fun onAudioVolumeIndication(volume: Int) {
            // Required to implement IMediaPlayerObserver
        }
    }

    // Interface to forward media player events to the UI
    interface MediaPlayerListener {
        fun onPlayerStateChanged(state: MediaPlayerState, error: MediaPlayerError)
        fun onProgress(percent: Int)
    }
}