package com.example.budget_budgie_opsc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Rive
import com.example.budget_budgie_opsc.databinding.ActivityExpensesScreenBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ExpensesScreen : AppCompatActivity() {

    private lateinit var currentUserId: String
    private lateinit var selectedAccountId: String
    private lateinit var binding: ActivityExpensesScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_expenses_screen)

        //Rive
        val ivan = findViewById<RiveAnimationView>(R.id.ivan)
        ivan.setRiveResource(AppData.currentOutfit)
        binding.ivan.controller.setBooleanState("Main", "isUnhappy", true)
        Rive.init(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                    val intent = Intent(this, GraphScreen::class.java)
                    startActivity(intent)
                    //Prevent the screen transition animation
                    overridePendingTransition(0, 0)
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

    fun addExpensesClicked(view: View){
        val intent = Intent(this, addExpensesScreen::class.java)
        startActivity(intent)
    }

    fun viewExpensesClicked(view: View){
        val intent = Intent(this, viewExpenseScreen::class.java)
        startActivity(intent)
    }


}