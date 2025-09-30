package com.example.budget_budgie_opsc

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
//import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(
    private var categories: List<Category>,
    private val onItemClicked: (Category) -> Unit // Listener for item clicks
) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvCategoryName)
        val amountAvailable: TextView = view.findViewById(R.id.tvAmountAvailable)
        val slider: Slider = view.findViewById(R.id.sliderAvailable)

        fun bind(category: Category) {
            name.text = category.name

            // --- Slider Configuration ---
            // Ensure valueFrom is always less than valueTo
            slider.valueFrom = 0f // This is usually fine, but ensure it's less than valueTo

            val budgetAsFloat = category.budget.toFloat()

            if (budgetAsFloat > slider.valueFrom) {
                slider.valueTo = budgetAsFloat
                // Set the current value of the slider.
                // This could be the total budget, or if you track spending,
                // it could be the remaining budget or current spending.
                // For now, let's assume it reflects the total budget available for this category.
                slider.value = budgetAsFloat
            } else {
                // Handle the case where budget is not > valueFrom (e.g., budget is 0)
                // Set a minimal valid range to prevent crashes
                slider.valueTo = slider.valueFrom + 1.0f // e.g., if valueFrom is 0, valueTo becomes 1
                slider.value = slider.valueFrom // Set value to the minimum
                Log.w("CategoryAdapter", "Category '${category.name}' has a budget of ${category.budget}, which is not greater than slider.valueFrom. Adjusted slider range.")
            }

            // --- Amount Available Text ---
            val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            amountAvailable.text = fmt.format(category.budget)

            // --- Item Click Listener ---
            itemView.setOnClickListener {
                onItemClicked(category) // Use the lambda passed to the constructor
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category) // Call the bind method in ViewHolder
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged() // For more complex lists, consider using DiffUtil
    }
}
