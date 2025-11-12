package com.example.budget_budgie_opsc

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class activity_account : AppCompatActivity() {

    private var userId: String = ""
    private lateinit var adapter: AccountAdapter
    private var cachedAccounts: List<Account> = emptyList()

    private lateinit var budgetUpdateContainer: LinearLayout
    private lateinit var etUpdateMinBudget: EditText
    private lateinit var etUpdateMaxBudget: EditText
    private lateinit var btnUpdateBudgets: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        userId = intent.getStringExtra("USER_ID") ?: ""

        val recyclerView = findViewById<RecyclerView>(R.id.accountRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AccountAdapter(emptyList(), { account ->
            setSelectedAccount(account.id)
            loadAccounts()
        })
        recyclerView.adapter = adapter

        budgetUpdateContainer = findViewById(R.id.budgetUpdateContainer)
        etUpdateMinBudget = findViewById(R.id.etUpdateMinBudget)
        etUpdateMaxBudget = findViewById(R.id.etUpdateMaxBudget)
        btnUpdateBudgets = findViewById(R.id.btnUpdateBudgets)

        btnUpdateBudgets.setOnClickListener {
            val selectedId = getSelectedAccount()
            if (selectedId.isEmpty()) {
                Toast.makeText(this, "Select an account first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val minBudget = etUpdateMinBudget.text.toString().toDoubleOrNull()
            val maxBudget = etUpdateMaxBudget.text.toString().toDoubleOrNull()
            if (minBudget == null || maxBudget == null) {
                Toast.makeText(this, "Enter both minimum and maximum budgets", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (minBudget <= 0 || maxBudget <= 0) {
                Toast.makeText(this, "Budgets must be greater than zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (maxBudget < minBudget) {
                Toast.makeText(this, "Maximum budget must be >= minimum budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                FirebaseServiceManager.accountService.updateBudgets(selectedId, minBudget, maxBudget)
                Toast.makeText(this@activity_account, "Budgets updated", Toast.LENGTH_SHORT).show()
                loadAccounts()
            }
        }

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
            val minBudget = findViewById<EditText>(R.id.etMinBudgetChecking).text.toString().toDoubleOrNull() ?: 0.0
            val maxBudget = findViewById<EditText>(R.id.etMaxBudgetChecking).text.toString().toDoubleOrNull() ?: 0.0
            lifecycleScope.launch {
                FirebaseServiceManager.accountService.insert(
                    Account(
                        userId = userId,
                        name = name.ifBlank { "Checking" },
                        balance = balance,
                        minBudget = minBudget,
                        maxBudget = maxBudget
                    )
                )
                loadAccounts()
                addForm.visibility = View.GONE
                clearInputs(
                    R.id.etNameChecking,
                    R.id.etBalanceChecking,
                    R.id.etMinBudgetChecking,
                    R.id.etMaxBudgetChecking
                )
            }
        }

        // Add Credit Card account
        findViewById<Button>(R.id.btnAddCredit).setOnClickListener {
            val name = findViewById<EditText>(R.id.etNameCredit).text.toString()
            val balance = findViewById<EditText>(R.id.etBalanceCredit).text.toString().toDoubleOrNull() ?: 0.0
            val minBudget = findViewById<EditText>(R.id.etMinBudgetCredit).text.toString().toDoubleOrNull() ?: 0.0
            val maxBudget = findViewById<EditText>(R.id.etMaxBudgetCredit).text.toString().toDoubleOrNull() ?: 0.0
            lifecycleScope.launch {
                FirebaseServiceManager.accountService.insert(
                    Account(
                        userId = userId,
                        name = name.ifBlank { "Credit Card" },
                        balance = balance,
                        minBudget = minBudget,
                        maxBudget = maxBudget
                    )
                )
                loadAccounts()
                addForm.visibility = View.GONE
                clearInputs(
                    R.id.etNameCredit,
                    R.id.etBalanceCredit,
                    R.id.etMinBudgetCredit,
                    R.id.etMaxBudgetCredit
                )
            }
        }

        // Add Debt account
        findViewById<Button>(R.id.btnAddDebt).setOnClickListener {
            val name = findViewById<EditText>(R.id.etNameDebt).text.toString()
            val balance = findViewById<EditText>(R.id.etBalanceDebt).text.toString().toDoubleOrNull() ?: 0.0
            val minBudget = findViewById<EditText>(R.id.etMinBudgetDebt).text.toString().toDoubleOrNull() ?: 0.0
            val maxBudget = findViewById<EditText>(R.id.etMaxBudgetDebt).text.toString().toDoubleOrNull() ?: 0.0
            lifecycleScope.launch {
                FirebaseServiceManager.accountService.insert(
                    Account(
                        userId = userId,
                        name = name.ifBlank { "Debt" },
                        balance = balance,
                        minBudget = minBudget,
                        maxBudget = maxBudget
                    )
                )
                loadAccounts()
                addForm.visibility = View.GONE
                clearInputs(
                    R.id.etNameDebt,
                    R.id.etBalanceDebt,
                    R.id.etMinBudgetDebt,
                    R.id.etMaxBudgetDebt
                )
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
            val accounts = FirebaseServiceManager.accountService.getAccountsForUser(userId)
            runOnUiThread {
                cachedAccounts = accounts
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
                if (getSelectedAccount().isEmpty() && accounts.isNotEmpty()) {
                    setSelectedAccount(accounts.first().id)
                }

                // 🔑 FIX: pass selected account id into adapter
                adapter.updateData(accounts, getSelectedAccount())

                updateNetTotal(accounts)
                updateSelectedAccountLabel(accounts)
                updateBudgetEditor(accounts.find { it.id == getSelectedAccount() })
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
    private fun setSelectedAccount(accountId: String) {
        val prefs = getSharedPreferences("BudgetBudgiePrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("SELECTED_ACCOUNT_ID", accountId).apply()
    }

    private fun getSelectedAccount(): String {
        val prefs = getSharedPreferences("BudgetBudgiePrefs", Context.MODE_PRIVATE)
        // Handle migration from Int to String
        if (prefs.contains("SELECTED_ACCOUNT_ID")) {
            val value = prefs.all["SELECTED_ACCOUNT_ID"]
            return when (value) {
                is String -> value
                is Int -> value.toString()
                else -> ""
            }
        }
        return ""
    }

    private fun updateBudgetEditor(account: Account?) {
        if (account == null) {
            budgetUpdateContainer.visibility = View.GONE
            etUpdateMinBudget.text?.clear()
            etUpdateMaxBudget.text?.clear()
            return
        }
        budgetUpdateContainer.visibility = View.VISIBLE
        etUpdateMinBudget.setText(if (account.minBudget > 0) account.minBudget.toString() else "")
        etUpdateMaxBudget.setText(if (account.maxBudget > 0) account.maxBudget.toString() else "")
    }

    private fun clearInputs(vararg ids: Int) {
        ids.forEach { id ->
            findViewById<EditText>(id)?.text?.clear()
        }
    }
}