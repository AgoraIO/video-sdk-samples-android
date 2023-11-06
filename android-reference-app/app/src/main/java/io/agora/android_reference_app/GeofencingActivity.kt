package io.agora.android_reference_app

import io.agora.geofencing_manager.GeofencingManager

class GeofencingActivity : BasicImplementationActivity() {
    private lateinit var geofencingManager: GeofencingManager

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_basic_implementation

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        geofencingManager = GeofencingManager(this)
        agoraManager = geofencingManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        geofencingManager.joinChannelWithToken()
    }
}