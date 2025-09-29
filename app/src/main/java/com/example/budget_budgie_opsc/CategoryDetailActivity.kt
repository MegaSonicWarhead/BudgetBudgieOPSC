package com.example.budget_budgie_opsc

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import java.text.NumberFormat
import java.util.Locale

class CategoryDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Category"
        val total = intent.getDoubleExtra("CATEGORY_TOTAL", 0.0)

        findViewById<TextView>(R.id.tvCategoryNameDetail).text = categoryName
        val fmt = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        findViewById<TextView>(R.id.tvTotalAmount).text = fmt.format(total)

        val slider = findViewById<Slider>(R.id.sliderDetail)
        slider.valueFrom = 0f
        slider.valueTo = total.toFloat()
        slider.value = total.toFloat()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCategorySettings).setOnClickListener {
            val intent = android.content.Intent(this, CategorySettingsActivity::class.java)
            intent.putExtra("CATEGORY_ID", intent.getIntExtra("CATEGORY_ID", -1))
            intent.putExtra("CATEGORY_NAME", categoryName)
            intent.putExtra("CATEGORY_TOTAL", total)
            startActivity(intent)
        }
    }
}


