package io.agora.android_reference_app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics

import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import io.agora.product_workflow_manager.ProductWorkflowManager

class ProductWorkflowActivity : BasicImplementationActivity() {
    private lateinit var productWorkflowManager: ProductWorkflowManager
    var selectedVolumeType = ProductWorkflowManager.VolumeTypes.PLAYBACK_SIGNAL_VOLUME
    var fgServiceIntent: Intent? = null
    var isSharingScreen = false

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_product_workflow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the UI elements
        setupVolumeSpinner()
        setupVolumeSeekbar()
        setupMuteCheckbox()
    }

    private fun setupMuteCheckbox() {
        val muteCheckBox = findViewById<CheckBox>(R.id.muteCheckBox)
        muteCheckBox.setOnCheckedChangeListener { _, isChecked ->
                productWorkflowManager.mute(isChecked)
        }
    }

    private fun setupVolumeSeekbar() {
        val seekBar = findViewById<SeekBar>(R.id.volumeSeekBar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // SeekBar value changed
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // This method is called when the user starts dragging the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // This method is called when the user stops dragging the SeekBar
                productWorkflowManager.adjustVolume(selectedVolumeType, seekBar.progress)
                showMessage("Setting ${selectedVolumeType.description} to ${seekBar.progress}")
            }
        })
    }

    private fun setupVolumeSpinner() {
        val volumeSpinner: Spinner = findViewById(R.id.volumeSpinner)

        // Create an array of enum values
        val volumeTypes = ProductWorkflowManager.VolumeTypes.values()

        // Create an array of strings for display
        val volumeTypeDescriptions = volumeTypes.map { it.description }.toTypedArray()

        // Create an adapter to populate the Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, volumeTypeDescriptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        volumeSpinner.adapter = adapter

        volumeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedVolumeType = volumeTypes[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected
            }
        }
    }

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        productWorkflowManager = ProductWorkflowManager(this)
        agoraManager = productWorkflowManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        productWorkflowManager.joinChannelWithToken()
    }

    fun shareScreen(view: View) {
        if (!productWorkflowManager.isJoined) {
            showMessage("Join a channel first")
            return
        }
        val button: Button = view as Button
        isSharingScreen = !isSharingScreen

        if (isSharingScreen) {
            // Ensure that your build version is Lollipop or higher.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    fgServiceIntent = Intent(this, BasicImplementationActivity::class.java)
                    ContextCompat.startForegroundService(this, fgServiceIntent!!)
                }
                // Get the screen metrics
                val metrics = DisplayMetrics()
                windowManager.getDefaultDisplay().getMetrics(metrics)
                productWorkflowManager.startScreenSharing(metrics)

                showScreenSharePreview();
                button.setText(getString(R.string.stop_screen_sharing))
            }
        } else {
            //productWorkflowManager.shareScreen(isSharingScreen)
            //setupLocalVideo()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (fgServiceIntent != null) stopService(fgServiceIntent)
            }
            productWorkflowManager.stopScreenSharing()
            button.setText(getString(R.string.start_screen_sharing))
        }
    }

    private fun showScreenSharePreview() {
        // Create a new FrameLayout
        var targetLayout = FrameLayout(applicationContext)
        // Set an onclick listener for video swapping
        targetLayout.setOnClickListener(videoClickListener)
        // Set the layout parameters for the new FrameLayout
        val layoutParams = LinearLayout.LayoutParams(
            400,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.setMargins(6, 6, 6, 6)
        // Set the id for the new FrameLayout
        targetLayout.id = View.generateViewId()
        // Add the new FrameLayout to the parent LinearLayout
        containerLayout.addView(targetLayout, layoutParams)
        var screenShareSurfaceView = productWorkflowManager.screenShareSurfaceView()
        // Add the SurfaceView to the FrameLayout
        targetLayout.addView(screenShareSurfaceView)
        // Associate an id with the FrameLayout for use in swapping
        targetLayout.tag = "-100"
        videoFrameMap[-100] = targetLayout
    }
}