package com.example.budget_budgie_opsc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategorySettingsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var categoryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_settings)

        db = AppDatabase.getDatabase(this)

        categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        val name = intent.getStringExtra("CATEGORY_NAME") ?: ""
        val amount = intent.getDoubleExtra("CATEGORY_TOTAL", 0.0)

        findViewById<TextView>(R.id.tvSettingsTitle).text = name
        findViewById<TextInputEditText>(R.id.etCategoryName).setText(name)
        findViewById<TextInputEditText>(R.id.etCategoryAmount).setText(amount.toString())

        findViewById<ImageButton>(R.id.btnBackSettings).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnSaveCategory).setOnClickListener {
            val newName = findViewById<TextInputEditText>(R.id.etCategoryName).text?.toString() ?: name
            val newAmount = findViewById<TextInputEditText>(R.id.etCategoryAmount).text?.toString()?.toDoubleOrNull() ?: amount

            CoroutineScope(Dispatchers.IO).launch {
                if (categoryId != -1) {
                    db.categoryDao().updateNameAndBudget(categoryId, newName, newAmount)
                }

                // Return the updated category info back to activity_category
                val resultIntent = Intent().apply {
                    putExtra("UPDATED_CATEGORY_ID", categoryId)
                    putExtra("UPDATED_CATEGORY_NAME", newName)
                    putExtra("UPDATED_CATEGORY_AMOUNT", newAmount)
                }

                runOnUiThread {
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }
}