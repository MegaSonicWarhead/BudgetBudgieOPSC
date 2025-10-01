package com.example.budget_budgie_opsc
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budget_budgie_opsc.databinding.ItemAccountBinding
import java.text.NumberFormat
import java.util.Locale

class AccountAdapter(private var accounts: List<Account>) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {
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
        holder.binding.tvAccountName.text = account.name
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        holder.binding.tvAccountBalance.text = fmt.format(account.balance)
    }

    override fun getItemCount() = accounts.size

    fun updateData(newAccounts: List<Account>) {
        accounts = newAccounts
        notifyDataSetChanged()
    }
}