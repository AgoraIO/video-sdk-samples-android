package io.agora.android_reference_app

import io.agora.play_media_manager.PlayMediaManager
import android.os.Bundle

class PlayMediaActivity : BasicImplementationActivity() {
    private lateinit var playMediaManager: PlayMediaManager
    private val baseListener = agoraManagerListener
    
    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_play_media

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up access to the UI elements
        
    }

    override fun initializeAgoraManager() {
        // Instantiate an object of the callQualityManager class, which is an extension of the AgoraManager
        playMediaManager = PlayMediaManager(this)
        agoraManager = playMediaManager
    }

    override fun join() {
        playMediaManager.joinChannelWithToken()
    }

    override fun leave() {
        super.leave()
    }
}