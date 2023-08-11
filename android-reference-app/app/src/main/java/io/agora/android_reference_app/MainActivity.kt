package io.agora.android_reference_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        setupRecyclerView()
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
        startActivity(intent)
    }
}
