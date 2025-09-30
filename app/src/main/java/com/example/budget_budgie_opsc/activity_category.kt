package com.example.budget_budgie_opsc

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.ui.semantics.text
//import androidx.glance.visibility
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class activity_category : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var userId: Int = -1
    private lateinit var adapter: CategoryAdapter // Assuming you have a CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        db = AppDatabase.getDatabase(this)
        userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            // Handle error: User ID not passed correctly
            Toast.makeText(this, "Error: User not identified.", Toast.LENGTH_LONG).show()
            finish() // Close the activity
            return
        }

        val recyclerView = findViewById<RecyclerView>(R.id.categoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Initialize your CategoryAdapter.
        // You'll need to pass a click listener or other interaction handlers if needed.
        adapter = CategoryAdapter(emptyList()) { category ->
            // Handle category item click, e.g., navigate to details or edit
        }
        recyclerView.adapter = adapter

        // Menu navigates back to Accounts
        findViewById<ImageButton>(R.id.btnMenuCategory).setOnClickListener {
            val intent = Intent(this, activity_account::class.java) // Ensure activity_account exists
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        // First-time budget form
        findViewById<Button>(R.id.btnConfirmBudget).setOnClickListener {
            val amountString = findViewById<TextInputEditText>(R.id.etMonthlyBudget).text?.toString()
            val amount = amountString?.toDoubleOrNull() ?: 0.0

            if (amount <= 0.0) {
                Toast.makeText(this, "Please enter a budget greater than 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Create a starter category, e.g., "General Budget" or similar
                db.categoryDao().insert(Category(userId = userId, name = "General", budget = amount))
                loadCategories()
            }
        }

        // Add category FAB (visible in list mode)
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            // For new categories, you might want to prompt the user for a name and budget,
            // or use a sensible default budget that is greater than 0.
            val defaultAmount = 1.0 // Use a default budget > 0 for the slider
            lifecycleScope.launch {
                // Consider prompting for a category name or using a placeholder
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
                    // Clear total available if no categories
                    findViewById<TextView>(R.id.tvTotalAvailable).text = ""
                } else {
                    form.visibility = View.GONE
                    list.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
                    adapter.updateData(categories) // Make sure CategoryAdapter has an updateData method
                    updateTotalAvailable(categories)
                }
            }
        }
    }

    private fun updateTotalAvailable(categories: List<Category>) {
        val total = categories.sumOf { it.budget }
        val tv = findViewById<TextView>(R.id.tvTotalAvailable)
        // Ensure tvTotalAvailable is present in your activity_category.xml
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        tv.text = fmt.format(total)
    }
}
