package io.agora.android_reference_app

import io.agora.rtc2.IRtcEngineEventHandler.RemoteVideoStats
import io.agora.call_quality_manager.CallQualityManager
import io.agora.play_media_manager.PlayMediaManager
import io.agora.call_quality_manager.CallQualityManager.CallQualityManagerListener

import android.graphics.Color
import android.widget.TextView
import android.os.Bundle
import android.view.SurfaceView
import android.widget.FrameLayout
import android.view.Gravity
import android.view.View
import android.widget.Button

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
        callQualityManager.joinChannelWithToken()
    }

    override fun leave() {
        super.leave()
    }
}