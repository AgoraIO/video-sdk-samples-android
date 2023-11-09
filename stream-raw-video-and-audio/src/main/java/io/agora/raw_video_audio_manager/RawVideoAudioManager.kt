package io.agora.raw_video_audio_manager

import android.content.Context
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineConfig.AreaCode.*
import java.lang.Exception

class RawVideoAudioManager(context: Context?) : AuthenticationManager(context) {

    override fun setupAgoraEngine(): Boolean {
        try {
            // Set the engine configuration
            val config = RtcEngineConfig()
            config.mContext = mContext
            config.mAppId = appId
            // Assign an event handler to receive engine callbacks
            config.mEventHandler = iRtcEngineEventHandler

            // Set the geofencing area code(s)
            config.mAreaCode = AREA_CODE_NA or AREA_CODE_EU // AreaCodes support bitwise operations
            sendMessage("Setting region for connection")

            // Create an RtcEngine instance
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            sendMessage(e.toString())
            return false
        }
        return true
    }
}