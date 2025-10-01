package com.example.budget_budgie_opsc

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import java.text.NumberFormat
import java.util.Locale

class CategoryDetailActivity : AppCompatActivity() {

    private var categoryId: Int = -1
    private var categoryName: String = ""
    private var total: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)

        // Get category data from intent
        categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Category"
        total = intent.getDoubleExtra("CATEGORY_TOTAL", 0.0)

        // Set UI
        findViewById<TextView>(R.id.tvCategoryNameDetail).text = categoryName
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        findViewById<TextView>(R.id.tvTotalAmount).text = fmt.format(total)

        val slider = findViewById<Slider>(R.id.sliderDetail)
        slider.valueFrom = 0f
        slider.valueTo = total.toFloat()
        slider.value = total.toFloat()

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Category Settings button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCategorySettings)
            .setOnClickListener {
                val intent = Intent(this, CategorySettingsActivity::class.java).apply {
                    putExtra("CATEGORY_ID", categoryId)
                    putExtra("CATEGORY_NAME", categoryName)
                    putExtra("CATEGORY_TOTAL", total)
                }
                startActivity(intent)
            }
    }
}