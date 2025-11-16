package com.example.budget_budgie_opsc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//Data class to represent a single shop item
data class ShopItem(
    val title: String,
    val points: Int,
    val imageResId: Int // Use a drawable resource ID for the image
)

//The Adapter class
class BudgieShopAdapter(private val items: List<ShopItem>) : RecyclerView.Adapter<BudgieShopAdapter.ShopViewHolder>() {

    //ViewHolder to hold the views for a single item
    class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.item_image)
        val itemTitle: TextView = view.findViewById(R.id.item_title)
        val itemPoints: TextView = view.findViewById(R.id.item_points)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        // Inflate the layout for a single item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousel_item_budgie_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        // Get the data for the current position
        val item = items[position]

        // Bind the data to the views
        holder.itemTitle.text = item.title
        holder.itemPoints.text = item.points.toString()
        holder.itemImage.setImageResource(item.imageResId)

        // You can add a click listener here if you want to make items buyable
        holder.itemView.setOnClickListener {
            // Handle item click, e.g., show a confirmation dialog
        }
    }

    override fun getItemCount() = items.size
}
