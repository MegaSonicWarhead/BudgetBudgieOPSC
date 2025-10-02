package com.example.budget_budgie_opsc

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class addExpensesScreen : AppCompatActivity() {

    private var categoryAll: String = "Categories"
    private var categoryOne: String = "Groceries"
    private var categoryTwo: String = "Gambling"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expenses_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val categoriesSpinner: Spinner = findViewById(R.id.spnrCategory)
        val categoriesOptions = listOf(categoryAll, categoryOne, categoryTwo)
        val categoriesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesOptions)
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = categoriesAdapter
    }


}