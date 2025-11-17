package com.example.budget_budgie_opsc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Rive
import com.example.budget_budgie_opsc.databinding.ActivityCategoryDetailBinding
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryDetailActivity : AppCompatActivity() {

    private var categoryId: String = ""
    private var categoryName: String = ""
    private var categoryTotal: Double = 0.0
    private var accountId: String = ""
    private var userId: String = ""

    private lateinit var binding: ActivityCategoryDetailBinding
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_category_detail)

        Rive.init(this)
        val ivan = findViewById<RiveAnimationView>(R.id.ivan)
        ivan.setRiveResource(AppData.currentOutfit)


        // Get category data from intent
        categoryId = intent.getStringExtra("CATEGORY_ID") ?: ""
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Category"
        categoryTotal = intent.getDoubleExtra("CATEGORY_TOTAL", 0.0)
        accountId = intent.getStringExtra("ACCOUNT_ID") ?: ""
        userId = intent.getStringExtra("USER_ID") ?: ""

        if (categoryId.isEmpty() || accountId.isEmpty() || userId.isEmpty()) {
            Toast.makeText(this, "Missing category information", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set UI
        binding.tvCategoryNameDetail.text = categoryName
        binding.tvTotalAmount.text = currencyFormatter.format(categoryTotal)
        binding.sliderDetail.apply {
            valueFrom = 0f
            valueTo = categoryTotal.toFloat().coerceAtLeast(1f)
            value = valueTo
            isEnabled = false
        }

        binding.tvCurrentAvailable.text = getString(
            R.string.category_remaining_amount,
            currencyFormatter.format(categoryTotal)
        )

        // Back button
        binding.btnBack.setOnClickListener { finish() }

        // Category Settings button
        binding.btnCategorySettings
            .setOnClickListener {
                val intent = Intent(this, CategorySettingsActivity::class.java).apply {
                    putExtra("CATEGORY_ID", categoryId)
                    putExtra("CATEGORY_NAME", categoryName)
                    putExtra("CATEGORY_TOTAL", categoryTotal)
                    putExtra("ACCOUNT_ID", accountId)
                    putExtra("USER_ID", userId)
                }
                startActivity(intent)
            }

        loadCategoryUsage()
    }

    override fun onResume() {
        super.onResume()
        loadCategoryUsage()
    }

    private fun loadCategoryUsage() {
        lifecycleScope.launch {
            val totalSpent = withContext(Dispatchers.IO) {
                FirebaseServiceManager.expenseService
                    .getExpensesByCategory(userId, accountId, categoryId)
                    .sumOf { it.amount }
            }

            val remaining = (categoryTotal - totalSpent).coerceAtLeast(0.0)

            callIvan(remaining, categoryTotal)

            binding.sliderDetail.apply {
                valueTo = categoryTotal.toFloat().coerceAtLeast(1f)
                value = remaining.toFloat().coerceIn(valueFrom, valueTo)
            }

            binding.tvCurrentAvailable.text = getString(
                R.string.category_remaining_amount,
                currencyFormatter.format(remaining)
            )
        }
    }


    private fun callIvan(moneyAvailable: Double, categoryTotal: Double) {
        val ivan = findViewById<RiveAnimationView>(R.id.ivan)

        if(moneyAvailable >= (categoryTotal/2)){
            binding.ivan.controller.setBooleanState("Main", "isHappy", true)
        }
        else if(moneyAvailable > (categoryTotal/4) && moneyAvailable < (categoryTotal/2)){
            binding.ivan.controller.setBooleanState("Main", "isHappy", false)
            binding.ivan.controller.setBooleanState("Main", "isIdle", true)
            binding.ivan.controller.setBooleanState("Main", "isIdle", false)
            binding.ivan.controller.setBooleanState("Main", "isUnhappy", true)
        }
        else if(moneyAvailable < (categoryTotal/4)){
            binding.ivan.controller.setBooleanState("Main", "isUnhappy", false)
            binding.ivan.controller.setBooleanState("Main", "isIdle", true)
            binding.ivan.controller.setBooleanState("Main", "isIdle", false)
            binding.ivan.controller.setBooleanState("Main", "isAngry", true)
        }
    }
}