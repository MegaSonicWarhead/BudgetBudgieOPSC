package com.example.budget_budgie_opsc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileScreen : AppCompatActivity() {

    private lateinit var currentUserId: String
    private lateinit var selectedAccountId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_budgiepage)

        // Get persisted userId and accountId
        val prefs = getSharedPreferences("BudgetBudgiePrefs", android.content.Context.MODE_PRIVATE)
        currentUserId = prefs.getString("USER_ID", "") ?: ""
        selectedAccountId = prefs.getString("SELECTED_ACCOUNT_ID", "") ?: ""
        
        if (currentUserId.isEmpty()) {
            // No logged-in user, go back to login
            startActivity(Intent(this, login::class.java))
            finish()
            return
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.nav_profile

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
                    val intent = Intent(this, GraphScreen::class.java)
                    startActivity(intent)
                    //Prevent the screen transition animation
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                    true
                }

                // If the ID doesn't match any of our cases, do nothing
                else -> false
            }
        }
    }
}