package io.agora.android_reference_app

data class ListItem(val title: String, val id: ExampleId) {
    enum class ExampleId {
        HEADER,
        SDK_QUICKSTART,
        AUTHENTICATION_WORKFLOW,
        CALL_QUALITY,
        PLAY_MEDIA,
        PRODUCT_WORKFLOW,
        CLOUD_PROXY,
        MEDIA_STREAM_ENCRYPTION,
        GEOFENCING,
        AUDIO_VOICE_EFFECTS,
        SPATIAL_AUDIO,
        VIRTUAL_BACKGROUND,
        CUSTOM_VIDEO_AUDIO,
        RAW_VIDEO_AUDIO,
        AI_NOISE_SUPPRESSION,
        LIVE_STREAMING_OVER_MULTIPLE_CHANNELS
    }
}

