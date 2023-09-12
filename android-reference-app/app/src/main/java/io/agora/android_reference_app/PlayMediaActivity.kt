package io.agora.android_reference_app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import io.agora.mediaplayer.Constants
import io.agora.play_media_manager.PlayMediaManager

class PlayMediaActivity : BasicImplementationActivity() {
    private lateinit var playMediaManager: PlayMediaManager
    private var mediaButton: Button? = null
    private var mediaProgressBar: ProgressBar? = null
    private var isMediaFileOpened = false

    // In a real world app, you declare the media location variable with an empty string
    // and update it when a user chooses a media file from a local or remote source.
    private val mediaLocation = "https://www.appsloveworld.com/wp-content/uploads/2018/10/640.mp4"

    //private val baseListener = agoraManagerListener

    private val mediaPlayerListener: PlayMediaManager.MediaPlayerListener
        get() = object : PlayMediaManager.MediaPlayerListener {
            override fun onPlayerStateChanged(
                state: Constants.MediaPlayerState,
                error: Constants.MediaPlayerError
            ) {
                when (state) {
                    Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                        isMediaFileOpened = true
                        runOnUiThread {
                            mediaButton?.setText(R.string.play_media_file)
                            mediaButton?.isEnabled = true
                            mediaProgressBar?.progress = 0
                        }
                    }
                    Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                        isMediaFileOpened = false
                        playMediaManager.destroyMediaPlayer()
                        runOnUiThread {
                            mediaButton?.setText(R.string.open_media_file)
                            mediaButton?.isEnabled = true
                            mediaProgressBar?.progress = 0
                        }
                    }
                    Constants.MediaPlayerState.PLAYER_STATE_FAILED -> {
                        runOnUiThread {
                            mediaButton?.setText(R.string.open_media_file)
                            mediaButton?.isEnabled = true
                            mediaProgressBar?.progress = 0
                        }
                    }
                    // Add more cases if needed
                    else -> {

                    }
                }
            }

            override fun onProgress(percent: Int) {
                    // Update the ProgressBar
                    mediaProgressBar?.progress = percent
            }
        }

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_play_media

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up access to the UI elements
        mediaProgressBar = findViewById(R.id.MediaProgress)
        mediaButton = findViewById(R.id.MediaPlayerButton)
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
    }

    fun playMedia(view: View) {
        if (!isMediaFileOpened) {
            playMediaManager.setupMediaPlayer(mediaPlayerListener)
            playMediaManager.openMediaFile(mediaLocation)
            mediaButton?.isEnabled = false
            mediaButton?.text = getString(R.string.opening_media_file)
        } else {
            val videoFrame = videoFrameMap[agoraManager.localUid]
            videoFrame?.removeAllViews()
            videoFrame?.addView(playMediaManager.videoSurfaceView())
            playMediaManager.playMedia()
        }
    }
}