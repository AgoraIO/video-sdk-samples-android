package io.agora.android_reference_app

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner

import io.agora.mediaplayer.Constants
import io.agora.product_workflow_manager.ProductWorkflowManager

class ProductWorkflowActivity : BasicImplementationActivity() {
    private lateinit var productWorkflowManager: ProductWorkflowManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_product_workflow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up access to the UI elements
        setUpVolumeSpinner()
    }

    private fun setUpVolumeSpinner() {
        val volumeSpinner: Spinner = findViewById(R.id.volumeSpinner)

        // Sample data for the spinner
        val options = listOf(
            "Recording signal volume",
            "Playback signal volume",
            "User playback signal volume",
            "Audio mixing volume",
            "Audio mixing play-out volume",
            "Audio mixing publish volume",
            "In-ear monitoring volume"
            )

        // Create an ArrayAdapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        volumeSpinner.adapter = adapter

        // Set a listener for spinner item selection
        volumeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = options[position]
                // Execute code based on the selected option
                showMessage(selectedOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
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