package io.agora.android_reference_app

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import io.agora.custom_video_audio_manager.CustomVideoAudioManager

class CustomVideoAudioActivity : BasicImplementationActivity() {
    private lateinit var customVideoAudioManager: CustomVideoAudioManager
    private var isPushingAudio = false

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_custom_video_audio

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        customVideoAudioManager = CustomVideoAudioManager(this)
        agoraManager = customVideoAudioManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        customVideoAudioManager.joinChannelWithToken()
    }

    @Suppress("UNUSED_PARAMETER")
    fun pushVideo(view: View) {
        customVideoAudioManager.setupCustomVideo()
        // Set up a preview TextureView for the custom video
        val previewTextureView = customVideoAudioManager.customLocalVideoPreview()

        // Show the preview TextureView
        previewTextureView!!.visibility = View.VISIBLE

        // Add the TextureView to the local video FrameLayout
        val videoFrame = videoFrameMap[agoraManager.localUid]
        videoFrame?.removeAllViews()
        videoFrame?.addView(previewTextureView,320,200)
    }

    fun pushAudio(view: View) {
        val button: Button = view as Button

        if (!isPushingAudio) {
            customVideoAudioManager.playCustomAudio()
            isPushingAudio = true
            button.text = getString(R.string.stop_pushing_custom_audio)
        } else {
            customVideoAudioManager.stopCustomAudio()
            isPushingAudio = false
            button.text = getString(R.string.start_pushing_custom_audio)
        }
    }
}