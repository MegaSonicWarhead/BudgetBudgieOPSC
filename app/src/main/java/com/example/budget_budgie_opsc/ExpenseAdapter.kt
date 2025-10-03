package com.example.budget_budgie_opsc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private var categoryMap: Map<Int, String> = emptyMap() // default empty map
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txt_expense_name_heading)
        val txtAmount: TextView = itemView.findViewById(R.id.txt_amount)
        val txtCategory: TextView = itemView.findViewById(R.id.txt_category)
        val imgReceipt: ImageView = itemView.findViewById(R.id.img_receipt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expense_item_layout, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.txtName.text = expense.description

        // Format amount as South African Rand
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        holder.txtAmount.text = formatter.format(expense.amount)

        // Show category name (fallback to ID if name missing)
        holder.txtCategory.text = categoryMap[expense.categoryId] ?: "Category ${expense.categoryId}"

        // Load receipt image if available
        if (!expense.receiptUri.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(expense.receiptUri)
                .into(holder.imgReceipt)
            holder.imgReceipt.visibility = View.VISIBLE
        } else {
            holder.imgReceipt.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = expenses.size

    // Update the list dynamically
    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    // Update category map dynamically
    fun updateCategoryMap(newCategoryMap: Map<Int, String>) {
        categoryMap = newCategoryMap
        notifyDataSetChanged() // refresh so category names update
    }
}