package io.agora.android_reference_app

import android.view.View
import android.widget.Button
import io.agora.raw_video_audio_manager.RawVideoAudioManager

class RawVideoAudioActivity : BasicImplementationActivity() {
    private lateinit var rawVideoAudioManager: RawVideoAudioManager
    private var isZoomed = false

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_raw_video_audio

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        rawVideoAudioManager = RawVideoAudioManager(this)
        agoraManager = rawVideoAudioManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        rawVideoAudioManager.joinChannelWithToken()
    }

    fun zoom(view: View){
        isZoomed = !isZoomed
        rawVideoAudioManager.setZoom(isZoomed)

        val button: Button = view as Button
        if (isZoomed) {
            button.text = getString(R.string.zoom_out)
        } else {
            button.text = getString(R.string.zoom_in)
        }
    }
}