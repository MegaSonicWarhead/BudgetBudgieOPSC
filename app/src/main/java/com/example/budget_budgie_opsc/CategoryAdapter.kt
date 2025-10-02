package com.example.budget_budgie_opsc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(
    private var categories: List<Category>,
    private val onItemClicked: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvCategoryName)
        val amountAvailable: TextView = view.findViewById(R.id.tvAmountAvailable)
        val slider: Slider = view.findViewById(R.id.sliderAvailable)

        fun bind(category: Category) {
            name.text = category.name

            slider.valueFrom = 0f
            val budgetAsFloat = category.allocatedAmount.toFloat()  // updated
            slider.valueTo = if (budgetAsFloat > 0f) budgetAsFloat else 1f
            slider.value = if (budgetAsFloat > 0f) budgetAsFloat else 0f

            val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            amountAvailable.text = fmt.format(category.allocatedAmount)  // updated

            itemView.setOnClickListener {
                onItemClicked(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}