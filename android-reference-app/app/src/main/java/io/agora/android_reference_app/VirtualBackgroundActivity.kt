package io.agora.android_reference_app

import android.os.Bundle
import android.widget.RadioGroup
import io.agora.virtual_background_manager.VirtualBackgroundManager

class VirtualBackgroundActivity : BasicImplementationActivity() {
    private lateinit var virtualBackgroundManager: VirtualBackgroundManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_virtual_background

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupBackground)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->

            when (checkedId) {
                R.id.optionNone -> {
                    virtualBackgroundManager.removeBackground()
                    showMessage("Virtual background turned off")
                }
                R.id.optionBlur -> {
                    virtualBackgroundManager.setBlurBackground()
                    showMessage("Blur background enabled")
                }
                R.id.optionSolid -> {
                    virtualBackgroundManager.setSolidBackground()
                    showMessage("Solid background enabled")
                }
                R.id.optionImage -> {
                    virtualBackgroundManager.setImageBackground()
                    showMessage("Image background enabled")
                }
            }
        }
    }

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        virtualBackgroundManager = VirtualBackgroundManager(this)
        agoraManager = virtualBackgroundManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        virtualBackgroundManager.joinChannelWithToken()

        if (virtualBackgroundManager.isFeatureAvailable()){
            showMessage("Your device supports virtual backgrounds")
        } else {
            showMessage("Your device does not support virtual backgrounds")
        }
    }
}