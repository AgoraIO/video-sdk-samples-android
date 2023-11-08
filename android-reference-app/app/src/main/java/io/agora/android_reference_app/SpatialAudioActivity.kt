package io.agora.android_reference_app

import android.os.Bundle
import android.widget.SeekBar
import io.agora.spatial_audio_manager.SpatialAudioManager

class SpatialAudioActivity : BasicImplementationActivity() {
    private lateinit var spatialAudioManager: SpatialAudioManager
    private var right: Float = 0f
    private var front: Float =0f
    private var top: Float =0f

    // Override the UI layout
    override val layoutResourceId: Int
        get() = R.layout.activity_spatial_audio

    override fun initializeAgoraManager() {
        // Instantiate an object of the PlayMediaManager
        spatialAudioManager = SpatialAudioManager(this)
        agoraManager = spatialAudioManager

        // Set up a listener for updating the UI
        agoraManager.setListener(agoraManagerListener)
    }

    override fun join() {
        spatialAudioManager.joinChannelWithToken()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frontSeekBar: SeekBar = findViewById(R.id.frontSeekBar)
        val rightSeekBar = findViewById<SeekBar>(R.id.rightSeekBar)
        val topSeekBar = findViewById<SeekBar>(R.id.topSeekBar)

        // Use the SeekBars to set coordinates of the remote user
        frontSeekBar.setOnSeekBarChangeListener(createSeekBarChangeListener { progress ->
            front = progress.toFloat()
        })

        rightSeekBar.setOnSeekBarChangeListener(createSeekBarChangeListener { progress ->
            right = progress.toFloat()
        })

        topSeekBar.setOnSeekBarChangeListener(createSeekBarChangeListener { progress ->
            top = progress.toFloat()
        })
    }

    private fun updateRemoteUserPosition() {
        if (spatialAudioManager.remoteUids.size>0) {
                spatialAudioManager.updateRemoteSpatialAudioPosition(
                    spatialAudioManager.remoteUids.first(),
                    front, right, top
                )
        }
    }

    private fun createSeekBarChangeListener(progressToValue: (Int) -> Unit): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Update the value when the user stops tracking
                progressToValue(seekBar?.progress ?: 0)
                updateRemoteUserPosition()
            }
        }
    }
}