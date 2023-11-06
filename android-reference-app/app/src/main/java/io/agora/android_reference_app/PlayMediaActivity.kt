package io.agora.android_reference_app

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.ProgressBar

import io.agora.mediaplayer.Constants
import io.agora.play_media_manager.PlayMediaManager

class PlayMediaActivity : BasicImplementationActivity() {
    private lateinit var playMediaManager: PlayMediaManager
    private var mediaButton: Button? = null
    private var mediaProgressBar: ProgressBar? = null

    // In a real world app, you declare the media location variable with an empty string
    // and update it when a user chooses a media file from a local or remote source.
    private val mediaLocation = "https://www.appsloveworld.com/wp-content/uploads/2018/10/640.mp4"

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_play_media

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up access to the UI elements
        mediaProgressBar = findViewById(R.id.MediaProgress)
        mediaButton = findViewById(R.id.MediaPlayerButton)
        mediaButton?.isEnabled = false
    }

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        playMediaManager = PlayMediaManager(this)
        agoraManager = playMediaManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        playMediaManager.joinChannelWithToken()
        mediaButton?.isEnabled = true
    }

    fun playMedia(view: View) {
        when (playMediaManager.mediaPlayerState()) {
            Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                // Play
                displayVideoInFrame(playMediaManager.mediaPlayerSurfaceView())
                playMediaManager.playMedia()
                mediaButton?.setText(R.string.pause)
            }
            Constants.MediaPlayerState.PLAYER_STATE_PLAYING -> {
                // Pause
                playMediaManager.pauseMedia()
                mediaButton?.setText(R.string.resume)
            }
            Constants.MediaPlayerState.PLAYER_STATE_PAUSED -> {
                // Resume
                playMediaManager.resumeMedia()
                mediaButton?.setText(R.string.pause)
            }
            else -> {
                // Open media file
                playMediaManager.setupMediaPlayer(mediaPlayerListener)
                playMediaManager.openMediaFile(mediaLocation)
                mediaButton?.isEnabled = false
                mediaButton?.text = getString(R.string.opening_media_file)
            }
        }
    }

    private val mediaPlayerListener: PlayMediaManager.MediaPlayerListener
        get() = object : PlayMediaManager.MediaPlayerListener {
            override fun onPlayerStateChanged(
                state: Constants.MediaPlayerState,
                error: Constants.MediaPlayerError
            ) {
                if (error != Constants.MediaPlayerError.PLAYER_ERROR_NONE) {
                    showMessage(error.name)
                }

                when (state) {
                    Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                        runOnUiThread {
                            // Ready to play, update button state
                            mediaButton?.setText(R.string.play_media_file)
                            mediaButton?.isEnabled = true
                            mediaProgressBar?.progress = 0
                        }
                    }
                    Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                        // Media finished playing
                        playMediaManager.destroyMediaPlayer()
                        runOnUiThread {
                            // Restore local video
                            displayVideoInFrame(playMediaManager.localVideo)
                            playMediaManager.updateChannelPublishOptions(false)
                            // Update the button caption and state
                            mediaButton?.setText(R.string.open_media_file)
                            mediaButton?.isEnabled = true
                            mediaProgressBar?.progress = 0
                        }
                    }
                    Constants.MediaPlayerState.PLAYER_STATE_FAILED -> {
                        runOnUiThread {
                            // Failed to open media file
                            showMessage("Failed to open media file.")
                            // Update the button caption and state
                            mediaButton?.setText(R.string.open_media_file)
                            mediaButton?.isEnabled = true
                            mediaProgressBar?.progress = 0
                        }
                    }
                    Constants.MediaPlayerState.PLAYER_STATE_PLAYING -> {
                        runOnUiThread {
                            // Update the button caption and state
                            mediaButton?.setText(R.string.pause)
                            mediaButton?.isEnabled = true
                        }
                    }
                    else -> {

                    }
                }
            }

            override fun onProgress(percent: Int) {
                // Update the ProgressBar
                mediaProgressBar?.progress = percent
            }
        }

    private fun displayVideoInFrame(surfaceView: SurfaceView) {
        // Add the SurfaceView to a FrameLayout
        val videoFrame = videoFrameMap[agoraManager.localUid]
        videoFrame?.removeAllViews()
        videoFrame?.addView(surfaceView)
        if (videoFrame == mainFrame) {
            surfaceViewMain = surfaceView
        }
    }

    override fun leave() {
        // Release media player resources
        playMediaManager.destroyMediaPlayer()
        // Update the button caption and state
        mediaButton?.isEnabled = false
        mediaButton?.setText(R.string.open_media_file)
        mediaProgressBar?.progress = 0
        super.leave()
    }

}