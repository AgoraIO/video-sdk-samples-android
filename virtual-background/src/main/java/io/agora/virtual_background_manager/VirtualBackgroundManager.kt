package io.agora.virtual_background_manager

import android.content.Context
import io.agora.authentication_manager.AuthenticationManager
import io.agora.rtc2.Constants
import io.agora.rtc2.video.SegmentationProperty
import io.agora.rtc2.video.VirtualBackgroundSource


class VirtualBackgroundManager(context: Context?) : AuthenticationManager(context) {

    fun isFeatureAvailable() :Boolean {
      return agoraEngine!!.isFeatureAvailableOnDevice(Constants.FEATURE_VIDEO_VIRTUAL_BACKGROUND)
    }

    fun setBlurBackground() {
        val virtualBackgroundSource = VirtualBackgroundSource()
        virtualBackgroundSource.backgroundSourceType = VirtualBackgroundSource.BACKGROUND_BLUR
        virtualBackgroundSource.blurDegree = VirtualBackgroundSource.BLUR_DEGREE_MEDIUM
        setBackground(virtualBackgroundSource)
    }

    fun setSolidBackground() {
        val virtualBackgroundSource = VirtualBackgroundSource()
        virtualBackgroundSource.backgroundSourceType = VirtualBackgroundSource.BACKGROUND_COLOR
        virtualBackgroundSource.color = 0x0000FF
        setBackground(virtualBackgroundSource)
    }

    fun setImageBackground() {
        val virtualBackgroundSource = VirtualBackgroundSource()
        virtualBackgroundSource.backgroundSourceType = VirtualBackgroundSource.BACKGROUND_IMG
        virtualBackgroundSource.source = "<absolute path to an image file>"
        setBackground(virtualBackgroundSource)
    }

    private fun setBackground(virtualBackgroundSource: VirtualBackgroundSource) {
        // Set processing properties for background
        val segmentationProperty = SegmentationProperty()
        segmentationProperty.modelType = SegmentationProperty.SEG_MODEL_AI
        // Use SEG_MODEL_GREEN if you have a green background
        segmentationProperty.greenCapacity =
            0.5f // Accuracy for identifying green colors (range 0-1)

        // Enable or disable virtual background
        agoraEngine!!.enableVirtualBackground(
            true,
            virtualBackgroundSource, segmentationProperty
        )
    }

    fun removeBackground() {
        // Disable virtual background
        agoraEngine!!.enableVirtualBackground(
            false,
            VirtualBackgroundSource(), SegmentationProperty()
        )
    }

}