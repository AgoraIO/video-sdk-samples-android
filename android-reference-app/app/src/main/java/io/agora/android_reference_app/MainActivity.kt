package io.agora.android_reference_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.agora_manager.AgoraManager.ProductName
import io.agora.agora_manager.AgoraManager

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private var selectedProduct = AgoraManager.ProductName.VIDEO_CALLING // Set default selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupProductSpinner()

        recyclerView = findViewById(R.id.recyclerView)
        setupRecyclerView()
    }

    private fun setupProductSpinner() {
        val productSpinner: Spinner = findViewById(R.id.productSpinner)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        for (productName in ProductName.values()) {
            val displayName = getDisplayName(productName)
            adapter.add(displayName)
        }

        productSpinner.adapter = adapter
        productSpinner.setSelection(adapter.getPosition(getDisplayName(selectedProduct)))

        productSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProduct = ProductName.values()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun getDisplayName(productName: ProductName): String {
        return when (productName) {
            ProductName.VIDEO_CALLING -> "Video Calling"
            ProductName.VOICE_CALLING -> "Voice Calling"
            ProductName.INTERACTIVE_LIVE_STREAMING -> "Interactive Live Streaming"
            ProductName.BROADCAST_STREAMING -> "Broadcast Streaming"
        }
    }


    private fun setupRecyclerView() {
        // Sample list of items
        val itemList = listOf(
            ListItem("GET STARTED", "header"),
            ListItem("Basic Implementation"),

            ListItem("DEVELOP", "header"),
            ListItem("Source Authentication"),
            ListItem("Call Quality"),
        )

        // Set up the adapter with the list of items and click listener
        val adapter = ItemListAdapter(itemList, object : ItemListAdapter.ItemClickListener {
            override fun onItemClick(item: ListItem) {
                when (item.title) {
                    "Basic Implementation" -> launchActivity(BasicImplementationActivity::class.java)
                    "Source Authentication" -> launchActivity(AuthenticationActivity::class.java)
                    "Call Quality" -> launchActivity(CallQualityActivity::class.java)
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
