package com.example.budget_budgie_opsc

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout

class ExpensesScreen : AppCompatActivity() {

    private var userId: Int = -1
    private var accountId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expenses_screen)

        // ✅ Receive the user and account from the previous activity
        userId = intent.getIntExtra("USER_ID", -1)
        accountId = intent.getIntExtra("ACCOUNT_ID", -1)

        // ✅ Handle tab clicks
        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutExpenses)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    "Categories" -> {
                        val intent = Intent(this@ExpensesScreen, activity_category::class.java)
                        intent.putExtra("USER_ID", userId)
                        intent.putExtra("ACCOUNT_ID", accountId)
                        startActivity(intent)
                        finish() // prevent activity stacking
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // ✅ Adjust layout for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun addExpensesClicked(view: View) {
        val intent = Intent(this, addExpensesScreen::class.java)
        intent.putExtra("USER_ID", userId)
        intent.putExtra("ACCOUNT_ID", accountId)
        startActivity(intent)
    }

    fun viewExpensesClicked(view: View) {
        val intent = Intent(this, viewExpenseScreen::class.java)
        intent.putExtra("USER_ID", userId)
        intent.putExtra("ACCOUNT_ID", accountId)
        startActivity(intent)
    }
}