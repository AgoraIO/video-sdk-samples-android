package io.agora.android_reference_app

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.*

import io.agora.mediaplayer.Constants
import io.agora.product_workflow_manager.ProductWorkflowManager

class ProductWorkflowActivity : BasicImplementationActivity() {
    private lateinit var productWorkflowManager: ProductWorkflowManager
    var selectedVolumeType = ProductWorkflowManager.VolumeTypes.PLAYBACK_SIGNAL_VOLUME

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_product_workflow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the UI elements
        setupVolumeSpinner()
        setupVolumeSeekbar()
    }

    private fun setupVolumeSeekbar() {
        val seekBar = findViewById<SeekBar>(R.id.volumeSeekBar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Handle the SeekBar value change here

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
        //mediaButton?.isEnabled = true
    }

    fun shareScreen(view: View) {

    }

    override fun leave() {

        super.leave()
    }

}