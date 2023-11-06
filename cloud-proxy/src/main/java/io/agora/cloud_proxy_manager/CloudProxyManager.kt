package io.agora.cloud_proxy_manager

import android.content.Context
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.Constants.*


class CloudProxyManager(context: Context?) : AuthenticationManager(context) {
    private var directConnectionFailed = false

    override fun connectionStateChanged(state: Int, reason: Int) {
        if (state ==  CONNECTION_STATE_FAILED
            && reason == CONNECTION_CHANGED_JOIN_FAILED ) {
            directConnectionFailed = true
            sendMessage("Join failed, reason: $reason")
        } else if (state == CONNECTION_CHANGED_SETTING_PROXY_SERVER) {
            sendMessage("Proxy server setting changed")
        }
    }

    override fun joinChannel(channelName: String, token: String?): Int {
        // Check if a proxy connection is required
        if (directConnectionFailed) {
            // Start cloud proxy service and set automatic UDP mode.
            val proxyStatus = agoraEngine!!.setCloudProxy(TRANSPORT_TYPE_UDP_PROXY)
            if (proxyStatus == 0) {
                sendMessage("Proxy service setup successful")
            } else {
                sendMessage("Proxy service setup failed with error :$proxyStatus")
            }
        }
        return super.joinChannel(channelName, token)
    }
}
