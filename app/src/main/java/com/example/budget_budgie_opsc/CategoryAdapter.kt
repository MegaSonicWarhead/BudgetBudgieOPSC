package com.example.budget_budgie_opsc
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider


class CategoryAdapter(private var categories: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvCategoryName)
        val amountAvailable: TextView = view.findViewById(R.id.tvAmountAvailable)
        val slider: Slider = view.findViewById(R.id.sliderAvailable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.name.text = category.name
        holder.slider.valueFrom = 0f
        holder.slider.valueTo = category.budget.toFloat()
        holder.slider.value = category.budget.toFloat()
        val fmt = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "ZA"))
        holder.amountAvailable.text = fmt.format(category.budget)

        holder.itemView.setOnClickListener { v ->
            val ctx = v.context
            val intent = android.content.Intent(ctx, CategoryDetailActivity::class.java)
            intent.putExtra("CATEGORY_NAME", category.name)
            intent.putExtra("CATEGORY_TOTAL", category.budget)
            ctx.startActivity(intent)
        }
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}