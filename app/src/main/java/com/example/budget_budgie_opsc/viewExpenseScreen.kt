package com.example.budget_budgie_opsc

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class viewExpenseScreen : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var dateSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var sortSpinner: Spinner
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button

    private val currentUserId = 1
    private val selectedAccountId = 1

    private lateinit var adapter: ExpenseAdapter
    private var categoriesList = listOf<Category>()
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_expense_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getDatabase(this)

        recyclerView = findViewById(R.id.recyclerExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(emptyList())
        recyclerView.adapter = adapter

        dateSpinner = findViewById(R.id.spnrDate)
        categorySpinner = findViewById(R.id.spnrCategories)
        sortSpinner = findViewById(R.id.spnrSort)
        startDateButton = findViewById(R.id.btnStartDate)
        endDateButton = findViewById(R.id.btnEndDate)

        setupSpinners()
        setupDatePickers()
        loadCategories()
        loadExpenses()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.nav_expenses

        bottomNavigationView.setOnItemSelectedListener { item ->
            // The 'item' variable is the menu item that was clicked
            when (item.itemId) {
                // Check which item was clicked by its ID from the menu file
                R.id.nav_categories -> {
                    val intent = Intent(this, activity_category::class.java)
                    intent.putExtra("USER_ID", currentUserId)
                    intent.putExtra("ACCOUNT_ID", selectedAccountId)
                    startActivity(intent)
                    overridePendingTransition(0, 0) // optional: disable animation
                    true
                }
                R.id.nav_expenses -> {
                    val intent = Intent(this, ExpensesScreen::class.java)
                    startActivity(intent)
                    //Prevent the screen transition animation
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_reports -> {
                    Toast.makeText(this, "Reports Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileScreen::class.java)
                    startActivity(intent)
                    //Prevent the screen transition animation
                    overridePendingTransition(0, 0)
                    true
                }

                // If the ID doesn't match any of our cases, do nothing
                else -> false
            }
        }
    }

    private fun setupSpinners() {
        // Date filter spinner
        val dateOptions = listOf("List All", "Select Period")
        val dateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateOptions)
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dateSpinner.adapter = dateAdapter

        // Sorting spinner
        val sortOptions = listOf(
            "Sort",
            "Date: Oldest to Newest",
            "Date: Newest to Oldest",
            "Amount: Highest to Lowest",
            "Amount: Lowest to Highest"
        )
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        // Unified listener to reload expenses when any filter changes
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (dateSpinner.selectedItemPosition == 1) {
                    startDateButton.visibility = View.VISIBLE
                    endDateButton.visibility = View.VISIBLE
                } else {
                    startDateButton.visibility = View.GONE
                    endDateButton.visibility = View.GONE
                    startDateMillis = null
                    endDateMillis = null
                }
                loadExpenses()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        dateSpinner.onItemSelectedListener = listener
        categorySpinner.onItemSelectedListener = listener
        sortSpinner.onItemSelectedListener = listener
    }

    private fun setupDatePickers() {
        startDateButton.setOnClickListener {
            showDatePicker { date ->
                startDateMillis = date.time
                startDateButton.text = dateFormat.format(date)
                loadExpenses()
            }
        }

        endDateButton.setOnClickListener {
            showDatePicker { date ->
                endDateMillis = date.time
                endDateButton.text = dateFormat.format(date)
                loadExpenses()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                onDateSelected(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            categoriesList = withContext(Dispatchers.IO) {
                db.categoryDao().getCategoriesForAccountAndUser(selectedAccountId, currentUserId)
            }

            val categoryNames = mutableListOf("All Categories")
            categoryNames.addAll(categoriesList.map { it.name })

            // Build a map of categoryId -> categoryName
            val categoryMap = categoriesList.associate { it.id to it.name }

            val adapterArray = ArrayAdapter(
                this@viewExpenseScreen,
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapterArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapterArray

            // Pass the categoryMap to the RecyclerView adapter
            adapter = ExpenseAdapter(emptyList(), categoryMap)
            recyclerView.adapter = adapter
        }
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            var expenses = withContext(Dispatchers.IO) {
                db.expenseDao().getExpensesForUserAccount(currentUserId, selectedAccountId)
            }

            // Filter by category
            val categoryPos = categorySpinner.selectedItemPosition
            if (categoryPos > 0) {
                val selectedCategory = categoriesList.getOrNull(categoryPos - 1)
                selectedCategory?.let { cat ->
                    expenses = expenses.filter { it.categoryId == cat.id }
                }
            }

            // Filter by date range
            startDateMillis?.let { start ->
                expenses = expenses.filter { expense -> expense.date >= start }
            }
            endDateMillis?.let { end ->
                expenses = expenses.filter { expense -> expense.date <= end }
            }

            // Sort
            expenses = when (sortSpinner.selectedItemPosition) {
                1 -> expenses.sortedBy { it.date }
                2 -> expenses.sortedByDescending { it.date }
                3 -> expenses.sortedByDescending { it.amount }
                4 -> expenses.sortedBy { it.amount }
                else -> expenses
            }

            adapter.updateData(expenses)
        }
    }
}