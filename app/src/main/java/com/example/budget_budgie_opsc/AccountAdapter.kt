package com.example.budget_budgie_opsc

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budget_budgie_opsc.databinding.ItemAccountBinding
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.util.Locale

class AccountAdapter(
    private var accounts: List<Account>,
    private val onAccountClick: (Account) -> Unit,
    private var selectedAccountId: Int = -1
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    inner class AccountViewHolder(val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]

        // Set name + balance
        holder.binding.tvAccountName.text = account.name
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        holder.binding.tvAccountBalance.text = fmt.format(account.balance)

        // Safely cast root to MaterialCardView
        val card = holder.binding.root as? MaterialCardView
        card?.let {
            if (account.id == selectedAccountId) {
                it.strokeWidth = 4
                it.strokeColor = Color.parseColor("#4CAF50") // green outline
            } else {
                it.strokeWidth = 0
                it.strokeColor = Color.TRANSPARENT
            }
        }

        // Handle clicks
        holder.binding.root.setOnClickListener {
            onAccountClick(account)
        }
    }

    override fun getItemCount() = accounts.size

    fun updateData(newAccounts: List<Account>, selectedId: Int) {
        accounts = newAccounts
        selectedAccountId = selectedId
        notifyDataSetChanged()
    }
}