package com.example.budget_budgie_opsc

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class activity_category : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var userId: Int = -1
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        db = AppDatabase.getDatabase(this)
        userId = intent.getIntExtra("USER_ID", -1)

        val recyclerView = findViewById<RecyclerView>(R.id.categoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter(emptyList())
        recyclerView.adapter = adapter

        // Menu navigates back to Accounts
        findViewById<ImageButton>(R.id.btnMenuCategory).setOnClickListener {
            val intent = Intent(this, activity_account::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        // First-time budget form
        findViewById<Button>(R.id.btnConfirmBudget).setOnClickListener {
            val amount = findViewById<TextInputEditText>(R.id.etMonthlyBudget).text?.toString()?.toDoubleOrNull() ?: 0.0
            lifecycleScope.launch {
                // Create a starter category, e.g., "General Budget" or similar
                db.categoryDao().insert(Category(userId = userId, name = "General", budget = amount))
                loadCategories()
            }
        }

        // Add category FAB (visible in list mode)
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            // Simple add another category with default budget using the same amount as General for now
            val defaultAmount = 0.0
            lifecycleScope.launch {
                db.categoryDao().insert(Category(userId = userId, name = "New Category", budget = defaultAmount))
                loadCategories()
            }
        }

        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getCategoriesForUser(userId)
            runOnUiThread {
                val isFirstTime = categories.isEmpty()
                val form = findViewById<View>(R.id.budgetFormContainer)
                val list = findViewById<RecyclerView>(R.id.categoryRecyclerView)
                val fab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddCategory)

                if (isFirstTime) {
                    form.visibility = View.VISIBLE
                    list.visibility = View.GONE
                    fab.visibility = View.GONE
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
        val total = categories.sumOf { it.budget }
        val tv = findViewById<TextView>(R.id.tvTotalAvailable)
        val fmt = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "ZA"))
        tv.text = fmt.format(total)
    }
}