package io.agora.android_reference_app

import io.agora.authentication_manager.AuthenticationManager
import io.agora.authentication_manager.AuthenticationManager.TokenCallback

import android.widget.EditText
import android.os.Bundle

class AuthenticationActivity : BasicImplementationActivity() {
    private lateinit var authenticationManager: AuthenticationManager
    private var editChannelName // To read the channel name from the UI.
            : EditText? = null
    private var editServerUrl // To read the server Url from the UI.
            : EditText? = null

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_authentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up access to the UI elements
        editChannelName = findViewById(R.id.editChannelName)
        editChannelName!!.setText(agoraManager.channelName)
        editServerUrl = findViewById(R.id.editServerUrl)
        editServerUrl!!.setText(authenticationManager.serverUrl)
    }

    override fun initializeAgoraManager() {
        // Instantiate an object of the AuthenticationManager class, which is an extension of the AgoraManager
        authenticationManager = AuthenticationManager(this)
        agoraManager = authenticationManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        // Read the channel name
        val channelName = editChannelName!!.text.toString()
        // Read the authentication server URL
        authenticationManager.serverUrl = editServerUrl!!.text.toString()
        if (authenticationManager.serverUrl == "") {
            showMessage("You must specify an token server URL")
            return
        }

        // Fetch a token from the server using an async call
        authenticationManager.fetchToken(channelName, object : TokenCallback {
            override fun onTokenReceived(rtcToken: String?) {
                // Join a channel using the token
                agoraManager.joinChannel(channelName, rtcToken)
            }

            override fun onError(errorMessage: String) {
                // Fetch token failed
                println("Error: $errorMessage")
            }
        })
    }
}