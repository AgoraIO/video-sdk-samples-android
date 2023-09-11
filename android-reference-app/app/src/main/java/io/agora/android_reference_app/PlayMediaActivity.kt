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

    // In a real world app, you declare the media location variable with an empty string
    // and update it when a user chooses a media file from a local or remote source.
    private val mediaLocation = "https://www.appsloveworld.com/wp-content/uploads/2018/10/640.mp4"

    private val baseListener = agoraManagerListener

    private val mediaPlayerListener: PlayMediaManager.MediaPlayerListener
        get() = object : PlayMediaManager.MediaPlayerListener {
            override fun onPlayerStateChanged(
                state: Constants.MediaPlayerState,
                error: Constants.MediaPlayerError
            ) {
                TODO("Not yet implemented")
            }

            override fun onPositionChanged(position: Long) {
                TODO("Not yet implemented")
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
        if (!playMediaManager.isMediaPlaying) {
            playMediaManager.openMediaFile(mediaLocation)
        } else {
           // playMediaManager.pauseMediaPlayer()
        }
    }
}