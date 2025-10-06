package com.example.budget_budgie_opsc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class activity_category : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var accountId: Int = -1
    private var currentUserId: Int = -1
    private lateinit var adapter: CategoryAdapter
    private var totalMonthlyBudget: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        db = AppDatabase.getDatabase(this)
        accountId = intent.getIntExtra("ACCOUNT_ID", -1)
        currentUserId = intent.getIntExtra("USER_ID", -1)

        if (accountId == -1 || currentUserId == -1) {
            Toast.makeText(this, "Error: Account or User not identified.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val recyclerView = findViewById<RecyclerView>(R.id.categoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = CategoryAdapter(emptyList()) { category ->
            val intent = if (category.name == "New Category") {
                Intent(this, CategorySettingsActivity::class.java).apply {
                    putExtra("CATEGORY_ID", category.id)
                    putExtra("CATEGORY_NAME", category.name)
                    putExtra("CATEGORY_TOTAL", category.allocatedAmount)
                    putExtra("USER_ID", currentUserId)
                }
            } else {
                Intent(this, CategoryDetailActivity::class.java).apply {
                    putExtra("CATEGORY_ID", category.id)
                    putExtra("CATEGORY_NAME", category.name)
                    putExtra("CATEGORY_TOTAL", category.allocatedAmount)
                    putExtra("USER_ID", currentUserId)
                }
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // 🔹 Burger menu -> Accounts screen
        findViewById<ImageButton>(R.id.btnMenuCategory).setOnClickListener {
            startActivity(Intent(this, activity_account::class.java).apply {
                putExtra("ACCOUNT_ID", accountId)
                putExtra("USER_ID", currentUserId)
            })
        }

        // 🔹 Go to Expenses screen (and remember user + account)
        findViewById<ImageButton>(R.id.btnGoExpenses).setOnClickListener {
            val intent = Intent(this, ExpensesScreen::class.java).apply {
                putExtra("USER_ID", currentUserId)
                putExtra("ACCOUNT_ID", accountId)
            }
            startActivity(intent)
        }

        // 🔹 Confirm monthly budget setup
        findViewById<Button>(R.id.btnConfirmBudget).setOnClickListener {
            val minStr = findViewById<TextInputEditText>(R.id.etMinBudget).text?.toString()
            val maxStr = findViewById<TextInputEditText>(R.id.etMaxBudget).text?.toString()

            val minAmount = minStr?.toDoubleOrNull() ?: 0.0
            val maxAmount = maxStr?.toDoubleOrNull() ?: 0.0

            if (minAmount <= 0 || maxAmount <= 0) {
                Toast.makeText(this, "Please enter budgets greater than 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minAmount > maxAmount) {
                Toast.makeText(this, "Minimum budget cannot be greater than maximum budget.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            totalMonthlyBudget = maxAmount

            lifecycleScope.launch {
                db.categoryDao().insert(
                    Category(
                        userId = currentUserId,
                        accountId = accountId,
                        name = "General",
                        allocatedAmount = minAmount
                    )
                )
                loadCategories()
            }
        }

        // 🔹 Add new category
        findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            val defaultBudget = 1.0

            lifecycleScope.launch {
                val categories = db.categoryDao().getCategoriesForAccountAndUser(accountId, currentUserId)
                val generalBudget = categories.firstOrNull { it.name == "General" }?.allocatedAmount ?: 0.0
                val used = categories.filter { it.name != "General" }.sumOf { it.allocatedAmount.toDouble() }
                val remainingBudget = generalBudget - used

                if (remainingBudget <= 0) {
                    runOnUiThread {
                        Toast.makeText(
                            this@activity_category,
                            "No budget remaining for new categories.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val categoryBudget = if (defaultBudget <= remainingBudget) defaultBudget else remainingBudget
                db.categoryDao().insert(
                    Category(
                        userId = currentUserId,
                        accountId = accountId,
                        name = "New Category",
                        allocatedAmount = categoryBudget
                    )
                )
                loadCategories()
            }
        }

        loadCategories()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getCategoriesForAccountAndUser(accountId, currentUserId)

            if (totalMonthlyBudget == 0.0) {
                val general = categories.firstOrNull { it.name == "General" }
                totalMonthlyBudget = general?.allocatedAmount ?: totalMonthlyBudget
            }

            runOnUiThread {
                val isFirstTime = categories.isEmpty()
                val form = findViewById<View>(R.id.budgetFormContainer)
                val list = findViewById<RecyclerView>(R.id.categoryRecyclerView)
                val fab = findViewById<FloatingActionButton>(R.id.fabAddCategory)

                if (isFirstTime) {
                    form.visibility = View.VISIBLE
                    list.visibility = View.GONE
                    fab.visibility = View.GONE
                    findViewById<TextView>(R.id.tvTotalAvailable).text = ""
                } else {
                    form.visibility = View.GONE
                    list.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
                    adapter.updateData(categories)
                    updateTotalAvailable(categories)
                }
            }
        }
    }

    private fun updateTotalAvailable(categories: List<Category>) {
        val generalBudget = categories.firstOrNull { it.name == "General" }?.allocatedAmount ?: 0.0
        val otherCategoriesUsed = categories
            .filter { it.name != "General" }
            .sumOf { it.allocatedAmount.toDouble() }

        val remaining = (generalBudget - otherCategoriesUsed).coerceAtLeast(0.0)
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        findViewById<TextView>(R.id.tvTotalAvailable).text = fmt.format(remaining)
    }
}