package io.agora.android_reference_app

import android.view.View
import io.agora.noise_suppression_manager.NoiseSuppressionManager

class NoiseSuppressionActivity : BasicImplementationActivity() {
    private lateinit var noiseSuppressionManager: NoiseSuppressionManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_basic_implementation

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        noiseSuppressionManager = NoiseSuppressionManager(this)
        agoraManager = noiseSuppressionManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        noiseSuppressionManager.joinChannelWithToken()
    }

}