package io.agora.spatial_audio_manager

import android.content.Context
import io.agora.authentication_manager.AuthenticationManager
import io.agora.spatialaudio.ILocalSpatialAudioEngine
import io.agora.spatialaudio.LocalSpatialAudioConfig
import io.agora.spatialaudio.RemoteVoicePositionInfo

class SpatialAudioManager(context: Context?) : AuthenticationManager(context) {
    // Instance of the spatial audio engine
    private var spatialAudioEngine: ILocalSpatialAudioEngine? = null

    private fun configureSpatialAudioEngine() {
        // Enable spatial audio
        agoraEngine!!.enableSpatialAudio(true)

        // Create and initialize the spatial audio engine
        val localSpatialAudioConfig = LocalSpatialAudioConfig()
        localSpatialAudioConfig.mRtcEngine = agoraEngine
        spatialAudioEngine = ILocalSpatialAudioEngine.create()
        spatialAudioEngine?.initialize(localSpatialAudioConfig)

        // Set the audio reception range of the local user in meters
        spatialAudioEngine?.setAudioRecvRange(50F)

        // Set the length of unit distance in meters
        spatialAudioEngine?.setDistanceUnit(1F)

        // Define the position of the local user
        val pos = floatArrayOf(0.0f, 0.0f, 0.0f)
        val forward = floatArrayOf(1.0f, 0.0f, 0.0f)
        val right = floatArrayOf(0.0f, 1.0f, 0.0f)
        val up = floatArrayOf(0.0f, 0.0f, 1.0f)
        // Set the position of the local user
        spatialAudioEngine?.updateSelfPosition(pos, forward, right, up)
    }

    fun updateRemoteSpatialAudioPosition(remoteUid: Int, front: Float, right: Float, top: Float) { //View view){
        // Define a remote user's spatial position
        val positionInfo = RemoteVoicePositionInfo()
        // The three values represent the front, right, and top coordinates
        positionInfo.position = floatArrayOf(front, right, top)
        positionInfo.forward = floatArrayOf(0.0f, 0.0f, -1.0f)

        // Update the spatial position of the specified remote user
        spatialAudioEngine?.updateRemotePosition(remoteUid, positionInfo)
        sendMessage("Spatial position of remote user ${remoteUid} updated")
    }

    override fun setupAgoraEngine(): Boolean {
        val result = super.setupAgoraEngine()
        
        // Setup the spatial audio engine
        configureSpatialAudioEngine()
        return result
    }
}