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
    private var userId: Int = -1
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        db = AppDatabase.getDatabase(this)
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: User not identified.", Toast.LENGTH_LONG).show()
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
                    putExtra("CATEGORY_TOTAL", category.budget)
                }
            } else {
                Intent(this, CategoryDetailActivity::class.java).apply {
                    putExtra("CATEGORY_ID", category.id)
                    putExtra("CATEGORY_NAME", category.name)
                    putExtra("CATEGORY_TOTAL", category.budget)
                }
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.btnMenuCategory).setOnClickListener {
            startActivity(Intent(this, activity_account::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }

        findViewById<Button>(R.id.btnConfirmBudget).setOnClickListener {
            val amountStr = findViewById<TextInputEditText>(R.id.etMonthlyBudget).text?.toString()
            val amount = amountStr?.toDoubleOrNull() ?: 0.0
            if (amount <= 0.0) {
                Toast.makeText(this, "Please enter a budget greater than 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                db.categoryDao().insert(Category(userId = userId, name = "General", budget = amount))
                loadCategories()
            }
        }

        findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            val defaultBudget = 1.0
            lifecycleScope.launch {
                val newCategory = Category(userId = userId, name = "New Category", budget = defaultBudget)
                db.categoryDao().insert(newCategory)
                loadCategories()
            }
        }

        loadCategories()
    }

    override fun onResume() {
        super.onResume()
        // Refresh categories whenever returning from settings or detail
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getCategoriesForUser(userId)
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
        val total = categories.sumOf { it.budget }
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        findViewById<TextView>(R.id.tvTotalAvailable).text = fmt.format(total)
    }
}