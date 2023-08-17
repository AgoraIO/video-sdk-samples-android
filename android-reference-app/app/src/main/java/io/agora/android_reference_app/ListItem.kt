package io.agora.android_reference_app

//data class ListItem(val title: String, val type: String = "")
data class ListItem(val title: String, val id: ExampleId) {
    enum class ExampleId {
        HEADER,
        SDK_QUICKSTART,
        AUTHENTICATION_WORKFLOW,
        CALL_QUALITY
    }
}

