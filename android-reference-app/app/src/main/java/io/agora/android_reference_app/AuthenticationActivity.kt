package io.agora.android_reference_app

import io.agora.android_reference_app.BasicImplementationActivity.onCreate
import io.agora.agora_manager.AgoraManager.channelName
import io.agora.authentication_manager.AuthenticationManager.serverUrl
import io.agora.agora_manager.AgoraManager.setListener
import io.agora.android_reference_app.BasicImplementationActivity.agoraManagerListener
import io.agora.authentication_manager.AuthenticationManager.fetchToken
import io.agora.agora_manager.AgoraManager.joinChannel
import io.agora.android_reference_app.BasicImplementationActivity
import io.agora.authentication_manager.AuthenticationManager
import android.widget.EditText
import io.agora.android_reference_app.R
import android.os.Bundle
import io.agora.authentication_manager.AuthenticationManager.TokenCallback

class AuthenticationActivity : BasicImplementationActivity() {
    private var authenticationManager: AuthenticationManager? = null
    private var editChannelName // To read the channel name from the UI.
            : EditText? = null
    private var editServerUrl // To read the server Url from the UI.
            : EditText? = null
    override val layoutResourceId: Int
        protected get() = R.layout.activity_authentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editChannelName = findViewById(R.id.editChannelName)
        editChannelName.setText(agoraManager!!.channelName)
        editServerUrl = findViewById(R.id.editServerUrl)
        editServerUrl.setText(authenticationManager!!.serverUrl)
    }

    override fun initializeAgoraManager() {
        authenticationManager = AuthenticationManager(this)
        agoraManager = authenticationManager

        // Set up a listener for updating the UI
        agoraManager!!.setListener(agoraManagerListener)
    }

    override fun join() {
        // Read the channel name
        val channelName = editChannelName!!.text.toString()
        // Read the authentication server URL
        authenticationManager!!.serverUrl = editServerUrl!!.text.toString()

        // Fetch a token from the server using an async call
        authenticationManager!!.fetchToken(channelName, object : TokenCallback {
            override fun onTokenReceived(rtcToken: String?) {
                // Join a channel using the token
                agoraManager!!.joinChannel(channelName, rtcToken)
            }

            override fun onError(errorMessage: String) {
                // Fetch token failed
                println("Error: $errorMessage")
            }
        })
    }
}