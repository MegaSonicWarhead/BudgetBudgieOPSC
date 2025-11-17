package com.example.budget_budgie_opsc

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Rive
import com.example.budget_budgie_opsc.databinding.ActivityBudgiepageBinding
import com.example.budget_budgie_opsc.databinding.ActivityLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ProfileScreen : AppCompatActivity() {

    private lateinit var currentUserId: String
    private lateinit var selectedAccountId: String

    private lateinit var shopItems: MutableList<ShopItem>
    private lateinit var shopAdapter: BudgieShopAdapter
    private var userPoints: Int = 0

    private lateinit var binding : ActivityBudgiepageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgiepageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_budgiepage)

        Rive.init(this)
//        val ivan = findViewById<RiveAnimationView>(R.id.ivan)
//        ivan.setRiveResource(AppData.currentOutfit)

        currentUserId = intent.getStringExtra("USER_ID") ?: ""
        selectedAccountId = intent.getStringExtra("ACCOUNT_ID") ?: ""

        setupShopCarousel()
        setupBottomNavigation()
        loadBudgetAndPoints()
    }

    private fun setupShopCarousel() {
        val recyclerView: RecyclerView = findViewById(R.id.budgie_shop_carousel)

        shopItems = mutableListOf(
            ShopItem("Ball & Chain", 100, R.drawable.ic_big_ball),
            ShopItem("Glasses", 75, R.drawable.ic_glasses),
            ShopItem("Winky Glasses", 120, R.drawable.ic_glasses2),
            ShopItem("Mother Russia", 80, R.drawable.ic_mother_russia),
            ShopItem("Cool Glasses", 50, R.drawable.ic_lightningglasses)
            // TODO: local DB
        )

        // --- Pass the handlePurchase function to the adapter ---
        shopAdapter = BudgieShopAdapter(shopItems) { item, position ->
            handlePurchase(item, position)
        }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = shopAdapter
    }

    private fun handlePurchase(item: ShopItem, position: Int) {
        //Check if the item is already owned
        if (item.isPurchased) {
            Toast.makeText(this, "You already own this item!", Toast.LENGTH_SHORT).show()
            //Change Ivan's Outfit
            val newIvanResource = AppData.GlobalIvanOutfits[item.title]
            val ivan = findViewById<RiveAnimationView>(R.id.loginCharacter)
            if(newIvanResource != null){
                ivan.setRiveResource(newIvanResource)
                AppData.currentOutfit = newIvanResource
            }
            AppData.GlobalOutfitAvailable[item.title] = true

            return
        }

        //Check if the user has enough points
        if (userPoints >= item.points) {
            //Purchase is successful
            //Deduct points
            userPoints -= item.points

            //Update the item's status
            item.isPurchased = true
            // TODO: local DB

            //Update the UI
            val pointsTextView: TextView = findViewById(R.id.pointsAmount)
            pointsTextView.text = userPoints.toString() // Update points display
            shopAdapter.updateItem(position) // Tell adapter to refresh this specific item

            //Update the stored points in SharedPreferences
            val prefs = getSharedPreferences("BudgetBudgiePrefs", MODE_PRIVATE).edit()
            prefs.putFloat("DAILY_POINTS", userPoints.toFloat())
            prefs.apply()

            Toast.makeText(this, "You purchased ${item.title}!", Toast.LENGTH_SHORT).show()

        } else {
            //Not enough points
            Toast.makeText(this, "Not enough points to buy ${item.title}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBudgetAndPoints() {
        val monthlyBudgetTextView: TextView = findViewById(R.id.monthlyBudget_Amount)
        val pointsTextView: TextView = findViewById(R.id.pointsAmount)

        if (selectedAccountId.isNotEmpty()) {
            lifecycleScope.launch {
                val account = withContext(Dispatchers.IO) {
                    FirebaseServiceManager.accountService.getAccountById(selectedAccountId)
                }
                account?.let {
                    monthlyBudgetTextView.text = formatCurrency(it.minBudget)
                }
            }
        }

        val prefs = getSharedPreferences("BudgetBudgiePrefs", MODE_PRIVATE)
        userPoints = prefs.getFloat("DAILY_POINTS", 0f).toInt()
        pointsTextView.text = userPoints.toString()
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "ZA")).format(amount)
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.nav_profile
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_categories, R.id.nav_expenses, R.id.nav_reports -> {
                    val targetClass = when (item.itemId) {
                        R.id.nav_categories -> activity_category::class.java
                        R.id.nav_expenses -> ExpensesScreen::class.java
                        else -> GraphScreen::class.java
                    }
                    val intent = Intent(this, targetClass).apply {
                        putExtra("USER_ID", currentUserId)
                        putExtra("ACCOUNT_ID", selectedAccountId)
                    }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}