package io.agora.noise_suppression_manager

import android.content.Context
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineConfig.AreaCode.*
import java.lang.Exception

class NoiseSuppressionManager(context: Context?) : AuthenticationManager(context) {

    override fun setupAgoraEngine(): Boolean {
        val result = super.setupAgoraEngine()

        // Enable AI noise suppression
        val mode = 2
        // Choose a noise suppression mode from the following:
        // 0: (Default) Balanced noise reduction mode
        // 1: Strong noise reduction mode
        // 2: Low latency and strong noise reduction mode
        agoraEngine!!.setAINSMode(true, mode)

        return result
    }
}