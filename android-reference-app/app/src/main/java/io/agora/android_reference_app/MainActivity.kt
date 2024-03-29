package io.agora.android_reference_app

import io.agora.agora_manager.AgoraManager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private var selectedProduct = AgoraManager.ProductName.VIDEO_CALLING // Set default selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupProductSpinner()

        recyclerView = findViewById(R.id.recyclerView)
        setupRecyclerView(selectedProduct)
    }

    private fun setupProductSpinner() {
        val productSpinner: Spinner = findViewById(R.id.productSpinner)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        for (productName in AgoraManager.ProductName.values()) {
            val displayName = getDisplayName(productName)
            adapter.add(displayName)
        }

        productSpinner.adapter = adapter
        productSpinner.setSelection(adapter.getPosition(getDisplayName(selectedProduct)))

        productSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProduct = AgoraManager.ProductName.values()[position]
                setupRecyclerView(selectedProduct)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun getDisplayName(productName: AgoraManager.ProductName): String {
        return when (productName) {
            AgoraManager.ProductName.VIDEO_CALLING -> "Video Calling"
            AgoraManager.ProductName.VOICE_CALLING -> "Voice Calling"
            AgoraManager.ProductName.INTERACTIVE_LIVE_STREAMING -> "Interactive Live Streaming"
            AgoraManager.ProductName.BROADCAST_STREAMING -> "Broadcast Streaming"
            else -> "Unknown Product"
        }
    }


    private fun setupRecyclerView(selectedProduct: AgoraManager.ProductName) {
        // Sample list of items
        val itemList = listOf(
            ListItem("GET STARTED", ListItem.ExampleId.HEADER),
            ListItem("SDK quickstart", ListItem.ExampleId.SDK_QUICKSTART),
            ListItem("Secure authentication with tokens", ListItem.ExampleId.AUTHENTICATION_WORKFLOW),

            ListItem("DEVELOP", ListItem.ExampleId.HEADER),
            ListItem("Call quality best practice", ListItem.ExampleId.CALL_QUALITY),
            ListItem("Stream media to a channel", ListItem.ExampleId.PLAY_MEDIA),
            ListItem("Screen share, volume control and mute", ListItem.ExampleId.PRODUCT_WORKFLOW),
            ListItem("Cloud proxy", ListItem.ExampleId.CLOUD_PROXY),
            ListItem("Media stream encryption", ListItem.ExampleId.MEDIA_STREAM_ENCRYPTION),
            ListItem("Custom video and audio", ListItem.ExampleId.CUSTOM_VIDEO_AUDIO),
            ListItem("Stream raw video and audio", ListItem.ExampleId.RAW_VIDEO_AUDIO),
            ListItem("Live streaming over multiple channels", ListItem.ExampleId.LIVE_STREAMING_OVER_MULTIPLE_CHANNELS),

            ListItem("INTEGRATE FEATURES", ListItem.ExampleId.HEADER),
            ListItem("Audio and voice effects", ListItem.ExampleId.AUDIO_VOICE_EFFECTS),
            ListItem("3D Spatial audio", ListItem.ExampleId.SPATIAL_AUDIO),
            ListItem("Geofencing", ListItem.ExampleId.GEOFENCING),
            ListItem("Virtual background", ListItem.ExampleId.VIRTUAL_BACKGROUND),
            ListItem("AI noise suppression", ListItem.ExampleId.AI_NOISE_SUPPRESSION),
        )

        // Filter the list based on the selected product
        val filteredItemList = when (selectedProduct) {
            AgoraManager.ProductName.VIDEO_CALLING -> {
                itemList.filter { item ->
                    item.id !in setOf(
                        ListItem.ExampleId.LIVE_STREAMING_OVER_MULTIPLE_CHANNELS
                    )
                }
            }
            AgoraManager.ProductName.VOICE_CALLING -> {
                itemList.filter { item ->
                    item.id !in setOf(
                        ListItem.ExampleId.LIVE_STREAMING_OVER_MULTIPLE_CHANNELS,
                        ListItem.ExampleId.VIRTUAL_BACKGROUND,
                    )
                }
            }
            else -> { // Interactive Live Streaming and Broadcast streaming
                itemList
            }
        }

        // Set up the adapter with the list of items and click listener
        val adapter = ItemListAdapter(filteredItemList, object : ItemListAdapter.ItemClickListener {
            override fun onItemClick(item: ListItem) {
                when (item.id) {
                    ListItem.ExampleId.SDK_QUICKSTART -> launchActivity(BasicImplementationActivity::class.java)
                    ListItem.ExampleId.AUTHENTICATION_WORKFLOW -> launchActivity(AuthenticationActivity::class.java)
                    ListItem.ExampleId.CALL_QUALITY -> launchActivity(CallQualityActivity::class.java)
                    ListItem.ExampleId.PLAY_MEDIA -> launchActivity(PlayMediaActivity::class.java)
                    ListItem.ExampleId.PRODUCT_WORKFLOW -> launchActivity(ProductWorkflowActivity::class.java)
                    ListItem.ExampleId.CLOUD_PROXY -> launchActivity(CloudProxyActivity::class.java)
                    ListItem.ExampleId.MEDIA_STREAM_ENCRYPTION -> launchActivity(MediaStreamEncryptionActivity::class.java)
                    ListItem.ExampleId.GEOFENCING -> launchActivity(GeofencingActivity::class.java)
                    ListItem.ExampleId.AUDIO_VOICE_EFFECTS -> launchActivity(AudioVoiceEffectsActivity::class.java)
                    ListItem.ExampleId.SPATIAL_AUDIO -> launchActivity(SpatialAudioActivity::class.java)
                    ListItem.ExampleId.VIRTUAL_BACKGROUND -> launchActivity(VirtualBackgroundActivity::class.java)
                    ListItem.ExampleId.CUSTOM_VIDEO_AUDIO -> launchActivity(CustomVideoAudioActivity::class.java)
                    ListItem.ExampleId.RAW_VIDEO_AUDIO -> launchActivity(RawVideoAudioActivity::class.java)
                    ListItem.ExampleId.LIVE_STREAMING_OVER_MULTIPLE_CHANNELS -> launchActivity(MultipleChannelsActivity::class.java)
                    ListItem.ExampleId.AI_NOISE_SUPPRESSION -> launchActivity(NoiseSuppressionActivity::class.java)
                    else -> {}
                }
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun launchActivity(activityClass: Class<*>) {
        // Launch the corresponding activity when an item is clicked
        val intent = Intent(applicationContext, activityClass)
        intent.putExtra("selectedProduct", selectedProduct.ordinal)
        startActivity(intent)
    }

}
