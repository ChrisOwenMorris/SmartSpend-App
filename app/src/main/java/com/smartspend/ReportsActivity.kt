package com.smartspend

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.annotation.SuppressLint

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.O)
class ReportsActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        // --- EXISTING NAVIGATION CODE --- //
        NavigationHelper.setupMenu(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavigationHelper.goToDashboard(this@ReportsActivity)
            }
        })

        // --- VIEWS --- //
        val btnWeek = findViewById<Button>(R.id.btnWeek)
        val btnMonth = findViewById<Button>(R.id.btnMonth)
        val btnYear = findViewById<Button>(R.id.btnYear)
        val tvPeriodLabel = findViewById<TextView>(R.id.tvPeriodLabel)
        val tvTotalAmount = findViewById<TextView>(R.id.tvTotalAmount)
        val tvComparison = findViewById<TextView>(R.id.tvComparison)
        val rvTopMerchants = findViewById<RecyclerView>(R.id.rvTopMerchants)
        val btnExport = findViewById<Button>(R.id.btnExport)

        // --- SETUP RECYCLERVIEW --- //
        rvTopMerchants.layoutManager = LinearLayoutManager(this)

        // --- LOAD DEFAULT VIEW (MONTH) --- //
        loadReport("month", tvPeriodLabel, tvTotalAmount, tvComparison, rvTopMerchants)

        // --- PERIOD BUTTON CLICKS --- //
        btnWeek.setOnClickListener {
            loadReport("week", tvPeriodLabel, tvTotalAmount, tvComparison, rvTopMerchants)
        }
        btnMonth.setOnClickListener {
            loadReport("month", tvPeriodLabel, tvTotalAmount, tvComparison, rvTopMerchants)
        }
        btnYear.setOnClickListener {
            loadReport("year", tvPeriodLabel, tvTotalAmount, tvComparison, rvTopMerchants)
        }

        // --- EXPORT BUTTON --- //
        btnExport.setOnClickListener {
            // PDF export can be added later
        }
    }

    private fun loadReport(
        period: String,
        tvPeriodLabel: TextView,
        tvTotalAmount: TextView,
        tvComparison: TextView,
        rvTopMerchants: RecyclerView
    ) {
        val today = LocalDate.now()
        val endDate = today.format(formatter)
        val daysBack: Long
        val label: String

        when (period) {
            "week" -> {
                daysBack = 7L
                label = getString(R.string.btn_week)
            }
            "year" -> {
                daysBack = 365L
                label = getString(R.string.btn_year)
            }
            else -> {
                daysBack = 30L
                label = getString(R.string.btn_month)
            }
        }

        val startDate = today.minusDays(daysBack).format(formatter)
        val prevStart = today.minusDays(daysBack * 2).format(formatter)

        lifecycleScope.launch {

            // GET TOTAL SPENDING
            val total = db.expenseDao().getTotalByDateRange(startDate, endDate)
            tvPeriodLabel.text = label
            tvTotalAmount.text = getString(R.string.amount_format, total)

            // GET PREVIOUS PERIOD FOR COMPARISON
            val prevTotal = db.expenseDao().getTotalByDateRange(prevStart, startDate)
            val change = if (prevTotal > 0) ((total - prevTotal) / prevTotal * 100) else 0.0
            val sign = if (change >= 0) "+" else ""
            tvComparison.text = getString(R.string.comparison_format, sign, change)

            // GET TOP CATEGORIES
            val topCategories = db.expenseDao().getTopCategoriesWithNames(startDate, endDate)
            rvTopMerchants.adapter = TopCategoriesAdapter(topCategories)
        }
    }
}