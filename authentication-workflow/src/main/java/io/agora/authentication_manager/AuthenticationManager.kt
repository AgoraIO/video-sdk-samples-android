package io.agora.authentication_manager

import android.content.Context
import okhttp3.Request.*
import io.agora.agora_manager.AgoraManager
import io.agora.rtc2.IRtcEngineEventHandler
import org.json.JSONObject
import org.json.JSONException
import okhttp3.*
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

open class AuthenticationManager(context: Context?) : AgoraManager(
    context!!
) {
    var serverUrl // The base URL to your token server
            : String
    private val tokenExpiryTime // Time in seconds after which the token will expire.
            : Int
    private val baseEventHandler: IRtcEngineEventHandler?

    // Callback interface to receive the http response from an async token request
    interface TokenCallback {
        fun onTokenReceived(rtcToken: String?)
        fun onError(errorMessage: String)
    }

    init {
        // Read the server url and expiry time from the config file
        serverUrl = config!!.optString("serverUrl")
        tokenExpiryTime = config!!.optInt("tokenExpiryTime", 600)
        baseEventHandler = super.iRtcEngineEventHandler
    }// Listen for a remote user joining the channel.// Handle the error// Use the token to renew// Get a new token

    // Listen for the event that the token is about to expire
    override val iRtcEngineEventHandler: IRtcEngineEventHandler
        get() = object : IRtcEngineEventHandler() {
            // Listen for the event that the token is about to expire
            override fun onTokenPrivilegeWillExpire(token: String) {
                sendMessage("Token is about to expire")
                // Get a new token
                fetchToken(channelName, object : TokenCallback {
                    override fun onTokenReceived(rtcToken: String?) {
                        // Use the token to renew
                        agoraEngine!!.renewToken(rtcToken)
                        sendMessage("Token renewed")
                    }

                    override fun onError(errorMessage: String) {
                        // Handle the error
                        sendMessage("Error: $errorMessage")
                    }
                })
                super.onTokenPrivilegeWillExpire(token)
            }

            // Listen for a remote user joining the channel.
            override fun onUserJoined(uid: Int, elapsed: Int) {
                baseEventHandler!!.onUserJoined(uid, elapsed)
            }

            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                baseEventHandler!!.onJoinChannelSuccess(channel, uid, elapsed)
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                baseEventHandler!!.onUserOffline(uid, reason)
            }
        }

    fun fetchToken(channelName: String, callback: TokenCallback) {
        fetchToken(channelName, config!!.optInt("uid"), callback)
    }

    fun fetchToken(channelName: String, uid: Int, callback: TokenCallback) {
        val tokenRole = if (isBroadcaster) 1 else 2
        // Prepare the Url
        val urlLString = (serverUrl + "/rtc/" + channelName + "/" + tokenRole + "/"
                + "uid" + "/" + uid + "/?expiry=" + tokenExpiryTime)
        val client = OkHttpClient()

        // Create a request
        val request: Request = Builder()
            .url(urlLString)
            .header("Content-Type", "application/json; charset=UTF-8")
            .get()
            .build()

        // Send the async http request
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            // Receive the response in a callback
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        // Extract rtcToken from the response
                        val responseBody = response.body!!.string()
                        val jsonObject = JSONObject(responseBody)
                        val rtcToken = jsonObject.getString("rtcToken")
                        // Return the token to the code that called fetchToken
                        callback.onTokenReceived(rtcToken)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        callback.onError("Invalid token response")
                    }
                } else {
                    callback.onError("Token request failed")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError("IOException: $e")
            }
        })
    }

    fun joinChannelWithToken(): Int {
        return joinChannelWithToken(channelName)
    }

    fun joinChannelWithToken(channelName: String): Int {
        if (agoraEngine == null) setupAgoraEngine()
        return if (isValidURL(serverUrl)) { // A valid server url is available
            // Fetch a token from the server for channelName
            fetchToken(channelName, object : TokenCallback {
                override fun onTokenReceived(rtcToken: String?) {
                    // Handle the received rtcToken
                    joinChannel(channelName, rtcToken)
                }

                override fun onError(errorMessage: String) {
                    // Handle the error
                    sendMessage("Error: $errorMessage")
                }
            })
            0
        } else { // use the token from the config.json file
            val token = config!!.optString("rtcToken")
            joinChannel(channelName, token)
        }
    }

    companion object {
        fun isValidURL(urlString: String?): Boolean {
            return try {
                // Attempt to create a URL object from the given string
                val url = URL(urlString)
                // Check if the URL's protocol and host are not empty
                url.protocol != null && url.host != null
            } catch (e: MalformedURLException) {
                // The given string is not a valid URL
                false
            }
        }
    }
}