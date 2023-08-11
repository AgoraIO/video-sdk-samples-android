package io.agora.android_reference_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemListAdapter(private val itemList: List<ListItem>, private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // Check the view type and inflate the corresponding layout
        val layoutResId = when (viewType) {
            "header".hashCode() -> R.layout.item_header_layout
            else -> R.layout.item_layout // You can have a default layout for other types
        }
        val itemView = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.titleTextView.text = currentItem.title

        if (currentItem.type != "header") {
            // Set click listener for the item
            holder.itemView.setOnClickListener {
                itemClickListener.onItemClick(currentItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // Use a unique identifier for each type of item.
        // Here, we can use the hash code of the type string as the identifier.
        return itemList[position].type.hashCode()
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface ItemClickListener {
        fun onItemClick(item: ListItem)
    }
}
