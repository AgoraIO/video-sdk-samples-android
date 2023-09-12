package io.agora.play_media_manager

import android.content.Context
import android.view.SurfaceView
import io.agora.authentication_manager.AuthenticationManager
import io.agora.mediaplayer.Constants.*
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.video.VideoCanvas


class PlayMediaManager(context: Context?) : AuthenticationManager(context) {
    private val baseEventHandler: IRtcEngineEventHandler = super.iRtcEngineEventHandler // Reuse the base class event handler
    private var mediaPlayer: IMediaPlayer? = null // Instance of the media player
    var isMediaPlaying = false
    private var mediaDuration: Long = 0
    private lateinit var mediaPlayerListener: MediaPlayerListener

    fun setupMediaPlayer(listener: MediaPlayerListener){
        if (mediaPlayer == null) {
            // Create an instance of the media player
            mediaPlayer = agoraEngine!!.createMediaPlayer()
            // Set the mediaPlayerObserver to receive callbacks
            mediaPlayer!!.registerPlayerObserver(mediaPlayerObserver)

            mediaPlayerListener = listener
            sendMessage("Opening media file...")
            return
        }
    }

    fun openMediaFile(mediaLocation: String) {
        if (mediaPlayer != null) {
            // Open the media file
            mediaPlayer?.open(mediaLocation, 0)
        }
    }

    fun pauseMediaPlayer() {
        // Set up the local video container to handle the media player output
        // or the camera stream, alternately.

        isMediaPlaying = !isMediaPlaying
        // Set the stream publishing options

//        updateChannelPublishOptions(isMediaPlaying)
        // Display the stream locally
        // Display the stream locally
//        setupLocalVideo(isMediaPlaying)

        val state = mediaPlayer?.state
        if (isMediaPlaying) { // Start or resume playing media
            if (state == MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                mediaPlayer?.play()
            } else if (state == MediaPlayerState.PLAYER_STATE_PAUSED) {
                mediaPlayer?.resume()
            }
            //mediaButton.setText("Pause Playing Media")
        } else {
            if (state == MediaPlayerState.PLAYER_STATE_PLAYING) {
                // Pause media file
                mediaPlayer?.pause()
              //  mediaButton.setText("Resume Playing Media")
            }
        }
    }

    fun destroyMediaPlayer(){
        // Destroy the media player
        if (mediaPlayer == null) return
        mediaPlayer!!.stop()
        mediaPlayer!!.unRegisterPlayerObserver(mediaPlayerObserver)
        mediaPlayer!!.destroy()
    }

    fun playMedia() {
        mediaPlayer?.play()
    }



    override val iRtcEngineEventHandler: IRtcEngineEventHandler
        get() = object : IRtcEngineEventHandler() {
            override fun onConnectionStateChanged(state: Int, reason: Int) {
                // Occurs when the network connection state changes
                baseEventHandler.onConnectionStateChanged(state, reason)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                baseEventHandler.onUserJoined(uid, elapsed)
            }

            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                baseEventHandler.onJoinChannelSuccess(channel, uid, elapsed)
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                baseEventHandler.onUserOffline(uid, reason)
            }

            override fun onTokenPrivilegeWillExpire(token: String) {
                baseEventHandler.onTokenPrivilegeWillExpire(token)
            }
        }


    private val mediaPlayerObserver: IMediaPlayerObserver = object : IMediaPlayerObserver {
        override fun onPlayerStateChanged(state: MediaPlayerState, error: MediaPlayerError) {
            sendMessage(state.toString())
            if (state == MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                // Media file opened successfully
                mediaDuration = mediaPlayer!!.duration
                mediaPlayerListener.onPlayerStateChanged(state, error)
                // Update the UI
             /*   UiThreadStatement.runOnUiThread(Runnable {
                    mediaButton.setText("Play Media File")
                    mediaButton.setEnabled(true)
                    mediaProgressBar.setProgress(0)
                } ) */
            } else if (state == MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED) {
                isMediaPlaying = false
                // Media file finished playing
              /*  UiThreadStatement.runOnUiThread(Runnable {
                    mediaButton.setText("Load Media File")
                    // Restore camera and microphone streams
                    setupLocalVideo(false)
                    updateChannelPublishOptions(false)
                }) */
                // Clean up
                mediaPlayer!!.destroy()
                mediaPlayer = null
            }
        }

        fun videoSurfaceView(): SurfaceView {
            var videoSurfaceView = SurfaceView(context)
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

    interface MediaPlayerListener {
        fun onPlayerStateChanged(state: MediaPlayerState, error: MediaPlayerError)
        fun onProgress(percent: Int)
    }
}