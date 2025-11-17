package com.example.budget_budgie_opsc

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ProfileScreen : AppCompatActivity() {

    private lateinit var currentUserId: String
    private lateinit var selectedAccountId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use the correct layout file that contains the RecyclerView
        setContentView(R.layout.activity_budgiepage)

        // It's good practice to retrieve any passed-in data at the start
        currentUserId = intent.getStringExtra("USER_ID") ?: ""
        selectedAccountId = intent.getStringExtra("ACCOUNT_ID") ?: ""

        if (currentUserId.isEmpty()) {

        }

        // Set up the main functionalities of this screen
        setupShopCarousel()
        setupBottomNavigation()
        loadBudgetAndPoints()
    }

    private fun setupShopCarousel() {
        val recyclerView: RecyclerView = findViewById(R.id.budgie_shop_carousel)

        val shopItems = listOf(
            ShopItem("Ball & Chain", 100, R.drawable.ic_big_ball),
            ShopItem("Glasses", 75, R.drawable.ic_glasses),
            ShopItem("Winky Glasses", 120, R.drawable.ic_glasses2),
            ShopItem("Knife Fight Memorabilia", 80, R.drawable.ic_mother_russia),
            ShopItem("Cool Glasses", 50, R.drawable.ic_lightningglasses)
        )

        val adapter = BudgieShopAdapter(shopItems)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        // Set the "Profile" item as selected
        bottomNavigationView.selectedItemId = R.id.nav_profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_categories -> {
                    val intent = Intent(this, activity_category::class.java)
                    intent.putExtra("USER_ID", currentUserId)
                    intent.putExtra("ACCOUNT_ID", selectedAccountId)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_expenses -> {
                    val intent = Intent(this, ExpensesScreen::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_reports -> {
                    val intent = Intent(this, GraphScreen::class.java)
                    intent.putExtra("USER_ID", currentUserId)
                    intent.putExtra("ACCOUNT_ID", selectedAccountId)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    // Already on this screen, do nothing
                    true
                }
                else -> false
            }
        }
    }

    private fun loadBudgetAndPoints() {
        val monthlyBudgetTextView: TextView = findViewById(R.id.monthlyBudget_Amount)
        val pointsTextView: TextView = findViewById(R.id.pointsAmount)

        // Load minimum budget for the currently selected account
        if (selectedAccountId.isNotEmpty()) {
            lifecycleScope.launch {
                val account = withContext(Dispatchers.IO) {
                    FirebaseServiceManager.accountService.getAccountById(selectedAccountId)
                }
                if (account != null) {
                    monthlyBudgetTextView.text = formatCurrency(account.minBudget)
                }
            }
        }

        // Load the latest daily points calculated on the Graph screen
        val prefs = getSharedPreferences("BudgetBudgiePrefs", MODE_PRIVATE)
        val storedPoints = prefs.getFloat("DAILY_POINTS", 0f)
        pointsTextView.text = storedPoints.toInt().toString()
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        return formatter.format(amount)
    }
}
