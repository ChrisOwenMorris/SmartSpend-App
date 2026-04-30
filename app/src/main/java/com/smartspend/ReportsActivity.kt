package com.smartspend

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.core.graphics.toColorInt

@RequiresApi(Build.VERSION_CODES.O)
class ReportsActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private var selectedMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    private var selectedCategoryId = 0
    private var selectedPeriod = "month"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        NavigationHelper.setupMenu(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavigationHelper.goToDashboard(this@ReportsActivity)
            }
        })

        setupViews()
        loadReportData()
    }

    private fun setupViews() {
        val btnWeek = findViewById<Button>(R.id.btnWeek)
        val btnMonth = findViewById<Button>(R.id.btnMonth)
        val btnYear = findViewById<Button>(R.id.btnYear)
        val etMonthPicker = findViewById<EditText>(R.id.etMonthPicker)
        val spCategoryFilter = findViewById<Spinner>(R.id.spCategoryFilter)
        val rvTopMerchants = findViewById<RecyclerView>(R.id.rvTopMerchants)
        val rvMonthlyEntries = findViewById<RecyclerView>(R.id.rvMonthlyEntries)

        rvTopMerchants.layoutManager = LinearLayoutManager(this)
        rvMonthlyEntries.layoutManager = LinearLayoutManager(this)

        setupMonthPicker(etMonthPicker)
        setupCategorySpinner(spCategoryFilter)

        btnWeek.setOnClickListener {
            selectedPeriod = "week"
            loadReportData()
        }
        btnMonth.setOnClickListener {
            selectedPeriod = "month"
            loadReportData()
        }
        btnYear.setOnClickListener {
            selectedPeriod = "year"
            loadReportData()
        }
    }

    private fun setupMonthPicker(editText: EditText) {
        editText.setText(selectedMonth)
        editText.setOnClickListener {
            val c = Calendar.getInstance()
            val monthStart = LocalDate.parse("${selectedMonth}-01")
            c.timeInMillis = monthStart.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()

            DatePickerDialog(
                this,
                { _, year, monthOfYear, _ ->
                    val monthStr = (monthOfYear + 1).toString().padStart(2, '0')
                    selectedMonth = "$year-$monthStr"
                    editText.setText(selectedMonth)
                    loadReportData()
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                1
            ).show()
        }
    }

    private fun setupCategorySpinner(spinner: Spinner) {
        val categories = listOf("All Categories", "Food", "Transport", "Shopping", "Bills", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedCategoryId = when (position) {
                    1 -> 1
                    2 -> 2
                    3 -> 3
                    4 -> 4
                    5 -> 5
                    else -> 0
                }
                loadReportData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadReportData() {
        val today = LocalDate.now()
        val monthStart = LocalDate.parse("${selectedMonth}-01")
        val monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth())

        val startDate = when (selectedPeriod) {
            "week" -> today.minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            "year" -> today.minusDays(365).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            else -> monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
        val endDate = when (selectedPeriod) {
            "week" -> today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            "year" -> today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            else -> monthEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }

        lifecycleScope.launch {
            val allExpenses = db.expenseDao().getExpensesByDateRange(startDate, endDate)
            val filteredExpenses = if (selectedCategoryId > 0) {
                allExpenses.filter { it.categoryId == selectedCategoryId }
            } else {
                allExpenses
            }

            val totalExpenses = filteredExpenses.sumOf { it.amount }
            val totalIncome = db.incomeDao().getTotalIncomeByDateRange(startDate, endDate)
            val topCategories = db.expenseDao().getTopCategoriesWithNames(startDate, endDate)
            val pieData = db.expenseDao().getExpensesGroupedByCategory(startDate, endDate)
            val sixMonthsAgo = today.minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val trendData = db.expenseDao().getMonthlyTrends(sixMonthsAgo)

            runOnUiThread {
                val categoryName = if (selectedCategoryId > 0) getCategoryName(selectedCategoryId) else "All Categories"
                findViewById<TextView>(R.id.tvPeriodLabel).text = "$categoryName - $selectedPeriod ($selectedMonth)"
                findViewById<TextView>(R.id.tvTotalAmount).text = "R %.2f".format(totalExpenses)
                findViewById<TextView>(R.id.tvComparison).text = "Net: R %.2f (Income: R %.2f)".format(totalIncome - totalExpenses, totalIncome)

                findViewById<IncomeExpenseBarChartView>(R.id.incomeExpenseChart).setData(totalIncome, totalExpenses)

                val colorPalette = listOf("#6A11CB", "#2575FC", "#FF5F6D", "#10B981", "#F59E0B").map { it.toColorInt() }
                val slices = pieData.take(5).mapIndexed { index, summary ->
                    com.smartspend.PieSlice(summary.categoryName ?: "Other", summary.total, colorPalette[index % colorPalette.size])
                }
                findViewById<com.smartspend.PieChartView>(R.id.pieChart).setData(slices)

                findViewById<com.smartspend.TrendChartView>(R.id.trendChart).setData(trendData)

                findViewById<RecyclerView>(R.id.rvTopMerchants).adapter = TopCategoriesAdapter(topCategories)
                findViewById<RecyclerView>(R.id.rvMonthlyEntries).adapter = MonthlyEntriesAdapter(filteredExpenses)
            }
        }
    }

    private fun getCategoryName(id: Int): String {
        return when (id) {
            1 -> "Food"
            2 -> "Transport"
            3 -> "Shopping"
            4 -> "Bills"
            5 -> "Other"
            else -> "Unknown"
        }
    }
}