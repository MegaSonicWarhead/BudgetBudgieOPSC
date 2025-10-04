package com.example.budget_budgie_opsc

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class addExpensesScreen : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var categoriesSpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var addImageButton: ImageButton
    private lateinit var removeImageButton: ImageButton
    private lateinit var dateButton: Button

    private val currentUserId = 1
    private val selectedAccountId = 1

    private var categoriesList = listOf<Category>()
    private var selectedImageUri: Uri? = null
    private var selectedDateMillis: Long? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expenses_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getDatabase(this)

        categoriesSpinner = findViewById(R.id.spnrCategory)
        descriptionEditText = findViewById(R.id.expense_edit_text)
        amountEditText = findViewById(R.id.amount_edit_text)
        submitButton = findViewById(R.id.button2)
        addImageButton = findViewById(R.id.btn_add_image)
        removeImageButton = findViewById(R.id.btn_remove_image)
        dateButton = findViewById(R.id.btnExpenseDate)

        loadCategories()

        addImageButton.setOnClickListener { pickImageFromGallery() }
        removeImageButton.setOnClickListener { clearSelectedImage() }
        dateButton.setOnClickListener { showDatePicker() }
        submitButton.setOnClickListener { saveExpense() }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            categoriesList = withContext(Dispatchers.IO) {
                db.categoryDao().getCategoriesForAccountAndUser(selectedAccountId, currentUserId)
            }

            val categoryNames = mutableListOf("Select Category")
            categoryNames.addAll(categoriesList.map { it.name })

            val adapter = ArrayAdapter(
                this@addExpensesScreen,
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categoriesSpinner.adapter = adapter
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                // Persist permission so you can still access the image later
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                addImageButton.setImageURI(uri)
                removeImageButton.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun clearSelectedImage() {
        selectedImageUri = null
        addImageButton.setImageResource(android.R.drawable.ic_input_add)
        removeImageButton.visibility = android.view.View.GONE
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                selectedDateMillis = cal.timeInMillis
                dateButton.text = "${dayOfMonth}/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun saveExpense() {
        val selectedPosition = categoriesSpinner.selectedItemPosition
        if (selectedPosition <= 0) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = categoriesList.getOrNull(selectedPosition - 1) ?: return
        val description = descriptionEditText.text.toString().trim()
        val amountText = amountEditText.text.toString().trim()

        if (description.isBlank() || amountText.isBlank() || selectedDateMillis == null) {
            Toast.makeText(this, "Please fill in all fields and select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            userId = currentUserId,
            accountId = selectedAccountId,
            categoryId = selectedCategory.id,
            description = description,
            amount = amount,
            date = selectedDateMillis!!,
            receiptUri = selectedImageUri?.toString()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            db.expenseDao().insertExpense(expense)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@addExpensesScreen, "Expense saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}