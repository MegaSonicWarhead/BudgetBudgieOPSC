package com.example.budget_budgie_opsc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

// Data class to represent a single shop item
data class ShopItem(
    val title: String,
    val points: Int,
    val imageResId: Int,
    var isPurchased: Boolean = false
)

// The Adapter class
class BudgieShopAdapter(
    private val items: List<ShopItem>,
    private val onItemClick: (ShopItem, Int) -> Unit // Lambda: (itemClicked, its_position)
) : RecyclerView.Adapter<BudgieShopAdapter.ShopViewHolder>() {

    // ViewHolder to hold the views for a single item
    class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.item_image)
        val itemTitle: TextView = view.findViewById(R.id.item_title)
        val itemPoints: TextView = view.findViewById(R.id.item_points)
        val itemCard: CardView = view as CardView // The root MaterialCardView
        val pointsIcon: ImageView = view.findViewById(R.id.points_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousel_item_budgie_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val item = items[position]

        holder.itemTitle.text = item.title

        // --- Handle purchased state ---
        if (item.isPurchased) {
            // If purchased: hide image/cost, change color
            holder.itemImage.visibility = View.INVISIBLE
            holder.itemPoints.visibility = View.INVISIBLE
            holder.pointsIcon.visibility = View.INVISIBLE
            holder.itemCard.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.green)
            )
        } else {
            // If not purchased: show everything as normal
            holder.itemImage.visibility = View.VISIBLE
            holder.itemPoints.visibility = View.VISIBLE
            holder.pointsIcon.visibility = View.VISIBLE
            holder.itemImage.setImageResource(item.imageResId)
            holder.itemPoints.text = item.points.toString()
            holder.itemCard.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            )
        }

        // Set the click listener to call the lambda function from the Activity
        holder.itemView.setOnClickListener {
            onItemClick(item, position)
        }
    }

    override fun getItemCount() = items.size

    // New method to update a single item in the list. This belongs to the Adapter class, not onBindViewHolder.
    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }
}
