package io.agora.android_reference_app

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Button

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