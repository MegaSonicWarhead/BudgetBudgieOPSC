package com.example.budget_budgie_opsc

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private var categoryMap: Map<Int, String> = emptyMap()
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txt_expense_name_heading)
        val txtAmount: TextView = itemView.findViewById(R.id.txt_amount)
        val txtCategory: TextView = itemView.findViewById(R.id.txt_category)
        val txtDate: TextView = itemView.findViewById(R.id.txt_date)
        val imgReceipt: ImageView = itemView.findViewById(R.id.img_receipt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expense_item_layout, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        // Description
        holder.txtName.text = expense.description

        // Format amount in South African Rand
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        holder.txtAmount.text = formatter.format(expense.amount)

        // Category
        holder.txtCategory.text = categoryMap[expense.categoryId] ?: "Category ${expense.categoryId}"

        // Format date (e.g. 04 Oct 2025)
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.txtDate.text = dateFormat.format(Date(expense.date))

        // Load receipt image safely
        if (!expense.receiptUri.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(Uri.parse(expense.receiptUri)) // ensure Uri is parsed
                .centerCrop()
                .into(holder.imgReceipt)
            holder.imgReceipt.visibility = View.VISIBLE
        } else {
            holder.imgReceipt.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    fun updateCategoryMap(newCategoryMap: Map<Int, String>) {
        categoryMap = newCategoryMap
        notifyDataSetChanged()
    }
}