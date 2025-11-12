package com.example.budget_budgie_opsc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

// Data class to hold information for each bar
data class BarData(val categoryName: String, val barValue: Int, val score: Int)


// These can be moved inside the class if they are not needed globally
private var currentUserId: String = ""
private var selectedAccountId: String = ""

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

    // --- State Management Properties ---
    private lateinit var allBarData: List<BarData>
    private var currentIndex = 0

    private fun getDayWithSuffix(day: Int): String {
        return when {
            day in 11..13 -> "${day}th"
            day % 10 == 1 -> "${day}st"
            day % 10 == 2 -> "${day}nd"
            day % 10 == 3 -> "${day}rd"
            else -> "${day}th"
        }
    }

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
        setupDropdownMenu()


        //Determine the current month name
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val currentMonthIndex = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        val currentMonthName = months[currentMonthIndex]

        //Load the data for the current month initially
        updateGraphForMonth(currentMonthName)

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
    }

    /**
     * Draws/refreshes the graph based on the currentIndex and the full dataset.
     */
    private fun drawGraph() {
        //Calculate maxValue from barValue
        val maxValue = allBarData.maxOfOrNull { it.barValue } ?: 150
        val visibleData = allBarData.drop(currentIndex).take(3)

        setupMeasurementAxis(maxValue)

        val allBars = listOf(bar1, bar2, bar3)
        val allDetails = listOf(
            Triple(bar1CatName, bar1Icon, bar1Value),
            Triple(bar2CatName, bar2Icon, bar2Value),
            Triple(bar3CatName, bar3Icon, bar3Value)
        )

        // Update visible bars and their details
        for (i in visibleData.indices) {
            val data = visibleData[i]
            val barView = allBars[i]
            val detailViews = allDetails[i]

            // Make views visible
            barView.visibility = View.VISIBLE
            detailViews.first.visibility = View.VISIBLE  // Category Name
            detailViews.second.visibility = View.VISIBLE // Icon
            detailViews.third.visibility = View.VISIBLE  // Value


            //Set bar height and category name
            setBarHeight(barView, data.barValue, maxValue)
            detailViews.first.text = data.categoryName

            //Format the score for display (e.g., "92/100")
            detailViews.third.text = "${data.score}/100"

            //Check the score and set the correct image
            if (data.score >= 70) {
                detailViews.second.setImageResource(R.drawable.ic_green_up)
            } else {
                detailViews.second.setImageResource(R.drawable.ic_red_up)
            }
        }

        // Hide any unused bars and their details
        for (i in visibleData.size until allBars.size) {
            allBars[i].visibility = View.INVISIBLE
            allDetails[i].first.visibility = View.INVISIBLE
            allDetails[i].second.visibility = View.INVISIBLE
            allDetails[i].third.visibility = View.INVISIBLE
        }

        // Update button visibility
        buttonPrevious.visibility = if (currentIndex > 0) View.VISIBLE else View.INVISIBLE
        buttonNext.visibility = if (currentIndex + 3 < allBarData.size) View.VISIBLE else View.INVISIBLE
}

    private fun setBarHeight(bar: View, value: Int, maxValue: Int) {
        val maxBarHeightInDp = 150
        val density = resources.displayMetrics.density
        val maxBarHeightInPixels = (maxBarHeightInDp * density).toInt()
        val newHeight = (value.toFloat() / maxValue.toFloat()) * maxBarHeightInPixels
        val currentParams = bar.layoutParams
        currentParams.height = newHeight.toInt()
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

    private fun setupMeasurementAxis(maxValue: Int) {
        maxValueTextView.text = maxValue.toString()
        midValueTextView.text = (maxValue / 2).toString()
        zeroValueTextView.text = "0"
    }

    private fun setupDropdownMenu() {
        //Create a list of months
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        //Set up the adapter with the list of months
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, months)
        val dropdownView: AutoCompleteTextView = findViewById(R.id.dropdown_items)
        dropdownView.setAdapter(adapter)

        //Set the initial text to the current month
        val currentMonthIndex = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        dropdownView.setText(months[currentMonthIndex], false)

        //Set the listener to handle month selection
        dropdownView.setOnItemClickListener { _, _, position, _ ->
            val selectedMonth = months[position]
            Toast.makeText(this, "Selected Month: $selectedMonth", Toast.LENGTH_SHORT).show()

            // Fetch new data for the selected month and redraw the graph
            updateGraphForMonth(selectedMonth)
        }
    }

    private fun updateGraphForMonth(monthName: String) {
        //This needs yo update from the database, RN it uses randomly generated data for the daily spending

        val calendar = java.util.Calendar.getInstance()
        // Set calendar to the selected month
        val monthIndex = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        ).indexOf(monthName)
        calendar.set(java.util.Calendar.MONTH, monthIndex)

        // Get the total number of days in that month
        val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

        // Generate sample data for each day of the month
        val dailyData = (1..daysInMonth).map { day ->
            val randomBarValue = (10..300).random() // Random expense value
            val randomScore = (20..100).random()    // Random score
            BarData(getDayWithSuffix(day), randomBarValue, randomScore)
        }

        // Update the master data list
        allBarData = dailyData
        // Reset the index to the beginning
        currentIndex = 0
        // Redraw the graph with the new daily data
        drawGraph()
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
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_reports -> true // Already on this screen
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileScreen::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }
}
