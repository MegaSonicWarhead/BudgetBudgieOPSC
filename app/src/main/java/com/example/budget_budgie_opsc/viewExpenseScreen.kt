package com.example.budget_budgie_opsc

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class viewExpenseScreen : AppCompatActivity() {

    private var categoryAll: String = "Categories"
    private var categoryOne: String = "Groceries"
    private var categoryTwo: String = "Gambling"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_expense_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val viewExpensesButton: Button = findViewById(R.id.btnViewExpenses)
        val expenseItemCard: CardView = findViewById(R.id.expense_item_card)

        val dateSpinner: Spinner = findViewById(R.id.spnrDate)
        val dateOptions = listOf("List All", "Select Period")
        val dateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateOptions)
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dateSpinner.adapter = dateAdapter

        val categoriesSpinner: Spinner = findViewById(R.id.spnrCategories)
        val categoriesOptions = listOf(categoryAll, categoryOne, categoryTwo)
        val categoriesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesOptions)
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = categoriesAdapter

        val sortSpinner: Spinner = findViewById(R.id.spnrSort)
        val sortOptions = listOf("Sort", "Date: Oldest to Newest", "Date: Newest to Oldest", "Amount: Highest to Lowest", "Amount: Lowest to Highest")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        // --- Container Reference ---
        val customDateContainer: CardView = findViewById(R.id.custom_date_container)
        val endDateContainer: CardView = findViewById(R.id.end_date_container)

        // --- Spinner Item Selection Listener ---
        dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Get the selected item as a string
                val selectedItem = parent.getItemAtPosition(position).toString()

                // Check if the selected item is "Custom Date"
                if (selectedItem == "Select Period") {
                    // If it is, make the container visible
                    customDateContainer.visibility = View.VISIBLE
                    endDateContainer.visibility = View.VISIBLE
                } else {
                    // For any other selection, make the container hidden
                    customDateContainer.visibility = View.GONE
                    endDateContainer.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing if no item is selected
            }
        }

        viewExpensesButton.setOnClickListener {
            expenseItemCard.visibility = View.VISIBLE
        }
    }

}