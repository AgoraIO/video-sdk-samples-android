package io.agora.media_stream_encryption_manager

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.internal.EncryptionConfig
import java.lang.Exception
import java.util.*

class MediaStreamEncryptionManager(context: Context?) : AuthenticationManager(context) {
    // A 32-byte string for encryption.
    private var encryptionKey = ""
    // A 32-byte string in Base64 format for encryption.
    private var encryptionSaltBase64 = ""

    init{
        // In a production environment, you retrieve the key and salt from
        // an authentication server. For this example you generate them locally.

        // Read the encryption key and salt from the config file
        encryptionKey = config!!.optString("cipherKey")
        encryptionSaltBase64 = config!!.optString("salt")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun enableEncryption() {
        if (encryptionSaltBase64.isBlank() || encryptionKey.isBlank()) return
        // Convert the salt string into bytes
        val encryptionSalt: ByteArray = Base64.getDecoder().decode(encryptionSaltBase64)
        // An object to specify encryption configuration.
        val config = EncryptionConfig()
        // Specify an encryption mode.
        config.encryptionMode = EncryptionConfig.EncryptionMode.AES_128_GCM2
        // Set secret key and salt.
        config.encryptionKey = encryptionKey
        System.arraycopy(
            encryptionSalt,
            0,
            config.encryptionKdfSalt,
            0,
            config.encryptionKdfSalt.size
        )
        // Call the method to enable media encryption.
        if (agoraEngine!!.enableEncryption(true, config) == 0) {
            sendMessage( "Media encryption enabled")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupAgoraEngine(): Boolean {
        try {
            // Set the engine configuration
            val config = RtcEngineConfig()
            config.mContext = mContext
            config.mAppId = appId
            // Assign an event handler to receive engine callbacks
            config.mEventHandler = iRtcEngineEventHandler

            // Create an RtcEngine instance
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine!!.enableVideo()

            // Enable encryption
            enableEncryption()
            sendMessage("Media encryption enabled")

        } catch (e: Exception) {
            sendMessage(e.toString())
            return false
        }
        return true
    }
}