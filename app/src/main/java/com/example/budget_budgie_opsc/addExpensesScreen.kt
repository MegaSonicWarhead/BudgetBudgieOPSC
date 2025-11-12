package com.example.budget_budgie_opsc

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

class addExpensesScreen : AppCompatActivity() {

    private lateinit var categoriesSpinner: Spinner
    private lateinit var descriptionEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var addImageButton: ImageButton
    private lateinit var removeImageButton: ImageButton
    private lateinit var dateButton: Button

    private var currentUserId: String = ""
    private var selectedAccountId: String = ""

    private var categoriesList = listOf<Category>()
    private var selectedImageUri: Uri? = null
    private var selectedDateMillis: Long? = null

    private val TAG = "IMG_PICK"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expenses_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUserId = intent.getStringExtra("USER_ID") ?: ""
        selectedAccountId = intent.getStringExtra("ACCOUNT_ID") ?: ""

        // FALLBACK: if empty, read from SharedPreferences
        if (currentUserId.isEmpty() || selectedAccountId.isEmpty()) {
            val prefs = getSharedPreferences("BudgetBudgiePrefs", android.content.Context.MODE_PRIVATE)
            if (currentUserId.isEmpty()) currentUserId = prefs.getString("USER_ID", "") ?: ""
            if (selectedAccountId.isEmpty()) selectedAccountId = prefs.getString("SELECTED_ACCOUNT_ID", "") ?: ""
        }

        // If STILL missing, force login/account selection
        if (currentUserId.isEmpty()) {
            startActivity(Intent(this, login::class.java))
            finish()
            return
        }
        if (selectedAccountId.isEmpty()) {
            startActivity(Intent(this, activity_account::class.java))
            finish()
            return
        }

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

    // Load categories from DB
    private fun loadCategories() {
        lifecycleScope.launch {
            categoriesList = withContext(Dispatchers.IO) {
                FirebaseServiceManager.categoryService.getCategoriesForAccountAndUser(selectedAccountId, currentUserId)
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

    // New-style image picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            Log.w(TAG, "No URI returned from picker")
            return@registerForActivityResult
        }

        Log.i(TAG, "Picked URI: $uri")

        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not persist permission: ${e.message}")
        }

        selectedImageUri = uri

        addImageButton.scaleType = ImageView.ScaleType.CENTER_CROP
        addImageButton.imageTintList = null // remove tint so image displays properly

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                safeDecodeBitmapFromUri(uri, 1024, 1024)
            }

            if (bitmap != null) {
                addImageButton.setImageBitmap(bitmap)
                removeImageButton.visibility = View.VISIBLE
                Log.i(TAG, "Image loaded successfully from URI.")
            } else {
                Log.w(TAG, "Manual decode failed — trying Coil fallback.")
                try {
                    addImageButton.load(uri) {
                        crossfade(true)
                        placeholder(android.R.drawable.ic_menu_gallery)
                        error(android.R.drawable.ic_menu_report_image)
                        // ✅ Coil listener without throwable.message
                        listener(
                            onError = { request, result ->
                                Log.e(TAG, "Coil failed to load image: $result")
                            },
                            onSuccess = { request, metadata ->
                                Log.i(TAG, "Coil loaded successfully")
                                removeImageButton.visibility = View.VISIBLE
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Coil fallback failed: ${e.message}", e)
                    Toast.makeText(
                        this@addExpensesScreen,
                        "Unable to load image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    // Decode image safely from content URI
    private fun safeDecodeBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                Log.w(TAG, "Invalid image bounds")
                return null
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap: ${e.message}", e)
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    // Remove selected image
    private fun clearSelectedImage() {
        selectedImageUri = null
        addImageButton.setImageResource(android.R.drawable.ic_input_add)
        addImageButton.imageTintList = null
        removeImageButton.visibility = View.GONE
    }

    // Date picker
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                selectedDateMillis = cal.timeInMillis
                dateButton.text = "$day/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    // Save expense
    private fun saveExpense() {
        val selectedPos = categoriesSpinner.selectedItemPosition
        if (selectedPos <= 0) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = categoriesList.getOrNull(selectedPos - 1) ?: return
        val description = descriptionEditText.text.toString().trim()
        val amountText = amountEditText.text.toString().trim()

        if (description.isBlank() || amountText.isBlank() || selectedDateMillis == null) {
            Toast.makeText(this, "Please fill in all fields and select a date", Toast.LENGTH_SHORT)
                .show()
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
            FirebaseServiceManager.expenseService.insertExpense(expense)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@addExpensesScreen, "Expense saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}