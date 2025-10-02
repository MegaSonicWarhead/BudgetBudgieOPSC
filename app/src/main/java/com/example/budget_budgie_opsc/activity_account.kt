package com.example.budget_budgie_opsc

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class activity_account : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var userId: Int = -1
    private lateinit var adapter: AccountAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        db = AppDatabase.getDatabase(this)
        userId = intent.getIntExtra("USER_ID", -1)

        val recyclerView = findViewById<RecyclerView>(R.id.accountRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AccountAdapter(emptyList(), { account ->
            setSelectedAccount(account.id)
            loadAccounts()
        })
        recyclerView.adapter = adapter

        val addButton = findViewById<Button>(R.id.addAccountButton)
        val bigFab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddLarge)
        val smallFab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd)
        val addForm = findViewById<View>(R.id.addFormContainer)

        val showAddForm: () -> Unit = {
            addForm.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            addButton.visibility = View.GONE
            bigFab.visibility = View.GONE
            smallFab.visibility = View.GONE
        }

        addButton.setOnClickListener { showAddForm() }
        bigFab.setOnClickListener { showAddForm() }
        smallFab.setOnClickListener { showAddForm() }

        // Navigate to Category screen
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            val intent = android.content.Intent(this, activity_category::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("ACCOUNT_ID", getSelectedAccount())
            startActivity(intent)
        }

        // Add Checking account
        findViewById<Button>(R.id.btnAddChecking).setOnClickListener {
            val name = findViewById<EditText>(R.id.etNameChecking).text.toString()
            val balance = findViewById<EditText>(R.id.etBalanceChecking).text.toString().toDoubleOrNull() ?: 0.0
            lifecycleScope.launch {
                db.accountDao().insert(Account(userId = userId, name = name.ifBlank { "Checking" }, balance = balance))
                loadAccounts()
                addForm.visibility = View.GONE
            }
        }

        // Add Credit Card account
        findViewById<Button>(R.id.btnAddCredit).setOnClickListener {
            val name = findViewById<EditText>(R.id.etNameCredit).text.toString()
            val balance = findViewById<EditText>(R.id.etBalanceCredit).text.toString().toDoubleOrNull() ?: 0.0
            lifecycleScope.launch {
                db.accountDao().insert(Account(userId = userId, name = name.ifBlank { "Credit Card" }, balance = balance))
                loadAccounts()
                addForm.visibility = View.GONE
            }
        }

        // Add Debt account
        findViewById<Button>(R.id.btnAddDebt).setOnClickListener {
            val name = findViewById<EditText>(R.id.etNameDebt).text.toString()
            val balance = findViewById<EditText>(R.id.etBalanceDebt).text.toString().toDoubleOrNull() ?: 0.0
            lifecycleScope.launch {
                db.accountDao().insert(Account(userId = userId, name = name.ifBlank { "Debt" }, balance = balance))
                loadAccounts()
                addForm.visibility = View.GONE
            }
        }

        // Cancel add form
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabBack).setOnClickListener {
            addForm.visibility = View.GONE
            loadAccounts()
        }

        loadAccounts()
    }

    private fun loadAccounts() {
        lifecycleScope.launch {
            val accounts = db.accountDao().getAccountsForUser(userId)
            runOnUiThread {
                val isFirstTime = accounts.isEmpty()
                val addButton = findViewById<Button>(R.id.addAccountButton)
                val bigFab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddLarge)
                val smallFab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd)
                val recycler = findViewById<RecyclerView>(R.id.accountRecyclerView)

                if (isFirstTime) {
                    addButton.visibility = View.VISIBLE
                    bigFab.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    smallFab.visibility = View.GONE
                } else {
                    addButton.visibility = View.GONE
                    bigFab.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                    smallFab.visibility = View.VISIBLE
                }

                // If no account selected → default to first
                if (getSelectedAccount() == -1 && accounts.isNotEmpty()) {
                    setSelectedAccount(accounts.first().id)
                }

                // 🔑 FIX: pass selected account id into adapter
                adapter.updateData(accounts, getSelectedAccount())

                updateNetTotal(accounts)
                updateSelectedAccountLabel(accounts)
            }
        }
    }

    private fun updateNetTotal(accounts: List<Account>) {
        val total = accounts.sumOf { it.balance }
        val tv = findViewById<TextView>(R.id.tvNetTotal)
        val fmt = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "ZA"))
        tv.text = fmt.format(total)
    }

    private fun updateSelectedAccountLabel(accounts: List<Account>) {
        val selectedId = getSelectedAccount()
        val selected = accounts.find { it.id == selectedId }
        val tv = findViewById<TextView>(R.id.tvSelectedAccount)
        tv.text = selected?.name ?: "No Account Selected"
    }

    // SharedPreferences for selected account
    private fun setSelectedAccount(accountId: Int) {
        val prefs = getSharedPreferences("BudgetBudgiePrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("SELECTED_ACCOUNT_ID", accountId).apply()
    }

    private fun getSelectedAccount(): Int {
        val prefs = getSharedPreferences("BudgetBudgiePrefs", Context.MODE_PRIVATE)
        return prefs.getInt("SELECTED_ACCOUNT_ID", -1)
    }
}