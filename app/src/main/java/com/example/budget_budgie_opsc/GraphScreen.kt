package com.example.budget_budgie_opsc

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Data class to hold information for each bar
data class BarData(val categoryName: String, val amount: Double)

class GraphScreen : AppCompatActivity() {

    // --- View Properties ---
    // Graph structure views
    private lateinit var bar1: View
    private lateinit var bar2: View
    private lateinit var bar3: View

    // Measurement axis views
    private lateinit var maxValueTextView: TextView
    private lateinit var midValueTextView: TextView
    private lateinit var zeroValueTextView: TextView

    // Date filter views
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var applyFilterButton: Button

    // Navigation button views
    private lateinit var buttonNext: ImageButton
    private lateinit var buttonPrevious: ImageButton

    // Bar 1 detail views
    private lateinit var bar1CatName: TextView
    private lateinit var bar1Icon: ImageView
    private lateinit var bar1Value: TextView

    // Bar 2 detail views
    private lateinit var bar2CatName: TextView
    private lateinit var bar2Icon: ImageView
    private lateinit var bar2Value: TextView

    // Bar 3 detail views
    private lateinit var bar3CatName: TextView
    private lateinit var bar3Icon: ImageView
    private lateinit var bar3Value: TextView

    // Budget information
    private lateinit var tvBudgetInfo: TextView
    private lateinit var tvDailyRecommendation: TextView
    private lateinit var tvDailyPoints: TextView

    // --- State Management Properties ---
    private lateinit var allBarData: List<BarData>
    private var currentIndex = 0
    private var currentUserId: String = ""
    private var selectedAccountId: String = ""
    private var startDateMillis: Long = 0L
    private var endDateMillis: Long = 0L
    private var minBudget: Double = 0.0
    private var maxBudget: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graph_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()

        currentUserId = intent.getStringExtra("USER_ID") ?: ""
        selectedAccountId = intent.getStringExtra("ACCOUNT_ID") ?: ""

        if (currentUserId.isEmpty() || selectedAccountId.isEmpty()) {
            val prefs = getSharedPreferences("BudgetBudgiePrefs", MODE_PRIVATE)
            if (currentUserId.isEmpty()) currentUserId = prefs.getString("USER_ID", "") ?: ""
            if (selectedAccountId.isEmpty()) selectedAccountId = prefs.getString("SELECTED_ACCOUNT_ID", "") ?: ""
        }

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

        setDefaultDateRange()
        updateDateLabels()

        startDateButton.setOnClickListener { showDatePicker(isStart = true) }
        endDateButton.setOnClickListener { showDatePicker(isStart = false) }
        applyFilterButton.setOnClickListener { loadGraphData() }

        loadBudgets()
        loadGraphData()

        buttonNext.setOnClickListener {
            // Move to the next set of data if possible
            if (currentIndex + 3 < allBarData.size) {
                currentIndex++
                drawGraph()
            }
        }
        buttonPrevious.setOnClickListener {
            // Move to the previous set of data if possible
            if (currentIndex > 0) {
                currentIndex--
                drawGraph()
            }
        }
        setupBottomNavigation()
    }

    /**
     * Initializes all view properties by finding them in the layout.
     */
    private fun initializeViews() {
        startDateButton = findViewById(R.id.btnStartDate)
        endDateButton = findViewById(R.id.btnEndDate)
        applyFilterButton = findViewById(R.id.btnApplyFilter)

        // Bars
        bar1 = findViewById(R.id.bar1)
        bar2 = findViewById(R.id.bar2)
        bar3 = findViewById(R.id.bar3)

        // Axis Labels
        maxValueTextView = findViewById(R.id.text_max_value)
        midValueTextView = findViewById(R.id.text_mid_value)
        zeroValueTextView = findViewById(R.id.text_zero_value)

        // Buttons
        buttonNext = findViewById(R.id.button_next)
        buttonPrevious = findViewById(R.id.button_previous)

        // Bar 1 Details
        bar1CatName = findViewById(R.id.bar1_category_name)
        bar1Icon = findViewById(R.id.bar1_icon)
        bar1Value = findViewById(R.id.bar1_value)

        // Bar 2 Details
        bar2CatName = findViewById(R.id.bar2_category_name)
        bar2Icon = findViewById(R.id.bar2_icon)
        bar2Value = findViewById(R.id.bar2_value)

        // Bar 3 Details
        bar3CatName = findViewById(R.id.bar3_category_name)
        bar3Icon = findViewById(R.id.bar3_icon)
        bar3Value = findViewById(R.id.bar3_value)

        tvBudgetInfo = findViewById(R.id.tvBudgetInfo)
        tvDailyRecommendation = findViewById(R.id.tvDailyRecommendation)
        tvDailyPoints = findViewById(R.id.tvDailyPoints)
    }

    /**
     * Draws/refreshes the graph based on the currentIndex and the full dataset.
     */
    private fun drawGraph() {
        if (allBarData.isEmpty()) {
            listOf(bar1, bar2, bar3).forEach { it.visibility = View.INVISIBLE }
            listOf(bar1CatName, bar2CatName, bar3CatName).forEach { it.visibility = View.INVISIBLE }
            listOf(bar1Icon, bar2Icon, bar3Icon).forEach { it.visibility = View.INVISIBLE }
            listOf(bar1Value, bar2Value, bar3Value).forEach { it.visibility = View.INVISIBLE }
            setupMeasurementAxis(0.0)
            buttonPrevious.visibility = View.INVISIBLE
            buttonNext.visibility = View.INVISIBLE
            return
        }

        val maxValue = allBarData.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0
        val visibleData = allBarData.drop(currentIndex).take(3)

        setupMeasurementAxis(maxValue)

        val allBars = listOf(bar1, bar2, bar3)
        val allDetails = listOf(
            Triple(bar1CatName, bar1Icon, bar1Value),
            Triple(bar2CatName, bar2Icon, bar2Value),
            Triple(bar3CatName, bar3Icon, bar3Value)
        )

        for (i in visibleData.indices) {
            val data = visibleData[i]
            val barView = allBars[i]
            val detailViews = allDetails[i]

            barView.visibility = View.VISIBLE
            detailViews.first.visibility = View.VISIBLE
            detailViews.second.visibility = View.VISIBLE
            detailViews.third.visibility = View.VISIBLE

            setBarHeight(barView, data.amount, maxValue)
            detailViews.first.text = data.categoryName
            detailViews.third.text = formatCurrency(data.amount)


            if(data.amount <= (minBudget/30)){
                detailViews.second.setImageResource(R.drawable.ic_green_up)
            }
            else{
                detailViews.second.setImageResource(R.drawable.ic_red_up)
            }

        }

        for (i in visibleData.size until allBars.size) {
            allBars[i].visibility = View.INVISIBLE
            allDetails[i].first.visibility = View.INVISIBLE
            allDetails[i].second.visibility = View.INVISIBLE
            allDetails[i].third.visibility = View.INVISIBLE
        }

        buttonPrevious.visibility = if (currentIndex > 0) View.VISIBLE else View.INVISIBLE
        buttonNext.visibility = if (currentIndex + 3 < allBarData.size) View.VISIBLE else View.INVISIBLE
    }

    private fun setBarHeight(bar: View, value: Double, maxValue: Double) {
        val maxBarHeightInDp = 150
        val density = resources.displayMetrics.density
        val maxBarHeightInPixels = (maxBarHeightInDp * density).toInt()
        val ratio = if (maxValue <= 0.0) 0f else (value.toFloat() / maxValue.toFloat())
        val newHeight = (ratio * maxBarHeightInPixels).toInt().coerceAtLeast(1)
        val currentParams = bar.layoutParams
        currentParams.height = newHeight
        bar.layoutParams = currentParams

        bar.post {
            val barBaseY = bar.y + bar.height
            val maxLabelY = barBaseY - maxBarHeightInPixels
            maxValueTextView.y = maxLabelY
            val topLabelCenterY = maxValueTextView.y + (maxValueTextView.height / 2)
            val bottomLabelCenterY = zeroValueTextView.y + (zeroValueTextView.height / 2)
            val midPointY = (topLabelCenterY + bottomLabelCenterY) / 2
            midValueTextView.y = midPointY - (midValueTextView.height / 2)
        }
    }

    private fun setupMeasurementAxis(maxValue: Double) {
        maxValueTextView.text = formatCurrency(maxValue)
        midValueTextView.text = formatCurrency(maxValue / 2)
        zeroValueTextView.text = formatCurrency(0.0)
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        return formatter.format(amount)
    }

    private fun setDefaultDateRange() {
        val calendar = Calendar.getInstance()
        endDateMillis = getStartOfDay(calendar.timeInMillis) + MILLIS_IN_DAY - 1
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        startDateMillis = getStartOfDay(calendar.timeInMillis)
    }

    private fun getStartOfDay(timeInMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun updateDateLabels() {
        startDateButton.text = formatDate(startDateMillis)
        endDateButton.text = formatDate(endDateMillis)
    }

    private fun formatDate(timestamp: Long): String {
        val formatter = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    private fun showDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (isStart) startDateMillis else endDateMillis
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                val selectedStart = getStartOfDay(cal.timeInMillis)
                if (isStart) {
                    startDateMillis = selectedStart
                    if (startDateMillis > endDateMillis) {
                        endDateMillis = startDateMillis + MILLIS_IN_DAY - 1
                    }
                } else {
                    endDateMillis = selectedStart + MILLIS_IN_DAY - 1
                    if (endDateMillis < startDateMillis) {
                        startDateMillis = getStartOfDay(endDateMillis)
                    }
                }
                updateDateLabels()
                loadGraphData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadBudgets() {
        lifecycleScope.launch {
            val account = withContext(Dispatchers.IO) {
                FirebaseServiceManager.accountService.getAccountById(selectedAccountId)
            }
            if (account != null) {
                minBudget = account.minBudget
                maxBudget = account.maxBudget
                tvBudgetInfo.text = getString(
                    R.string.graph_budget_info,
                    formatCurrency(minBudget),
                    formatCurrency(maxBudget)
                )
                updateDailySummary(0.0)
            } else {
                tvBudgetInfo.text = getString(R.string.graph_budget_info_missing)
                tvDailyRecommendation.text = ""
                tvDailyPoints.text = ""
            }
        }
    }

    private fun loadGraphData() {
        lifecycleScope.launch {
            val categories = withContext(Dispatchers.IO) {
                FirebaseServiceManager.categoryService.getCategoriesForAccountAndUser(selectedAccountId, currentUserId)
            }
            val expenses = withContext(Dispatchers.IO) {
                FirebaseServiceManager.expenseService.getExpensesForUserAccount(currentUserId, selectedAccountId)
            }

            val categoryMap = categories.associateBy { it.id }
            val filteredExpenses = expenses.filter { it.date in startDateMillis..endDateMillis }

            val totals = filteredExpenses
                .groupBy { it.categoryId }
                .map { (categoryId, list) ->
                    val name = categoryMap[categoryId]?.name ?: getString(R.string.graph_unknown_category)
                    BarData(name, list.sumOf { it.amount })
                }
                .sortedByDescending { it.amount }

            allBarData = totals
            currentIndex = 0
            drawGraph()

            val todayStart = getStartOfDay(System.currentTimeMillis())
            val todayEnd = todayStart + MILLIS_IN_DAY - 1
            val todaysTotal = filteredExpenses
                .filter { it.date in todayStart..todayEnd }
                .sumOf { it.amount }
            updateDailySummary(todaysTotal)
        }
    }

    private fun updateDailySummary(todaysTotal: Double) {
        if (minBudget <= 0.0) {
            tvDailyRecommendation.text = getString(R.string.graph_daily_target_missing)
            tvDailyPoints.text = ""
            return
        }

        val dailyTarget = minBudget / 30.0
        tvDailyRecommendation.text = getString(
            R.string.graph_daily_recommendation,
            formatCurrency(dailyTarget)
        )

        val points = if (todaysTotal <= dailyTarget) {
            val ratio = (dailyTarget - todaysTotal) / dailyTarget
            (ratio * 100).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
        tvDailyPoints.text = getString(
            R.string.graph_daily_points,
            formatCurrency(todaysTotal),
            points.toInt()
        )
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.nav_reports
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
                    intent.putExtra("USER_ID", currentUserId)
                    intent.putExtra("ACCOUNT_ID", selectedAccountId)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_reports -> true // Already on this screen
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileScreen::class.java)
                    intent.putExtra("USER_ID", currentUserId)
                    intent.putExtra("ACCOUNT_ID", selectedAccountId)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        private const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L
    }
}
