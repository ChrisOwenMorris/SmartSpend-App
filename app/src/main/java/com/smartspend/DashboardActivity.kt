package com.smartspend

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.smartspend.data.entity.Expense
import com.smartspend.data.entity.Income

class DashboardActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private lateinit var rvRecentExpenses: RecyclerView
    private lateinit var etMonthSelector: EditText
    private var selectedMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        NavigationHelper.setupMenu(this)

        rvRecentExpenses = findViewById(R.id.rvRecentExpenses)
        etMonthSelector = findViewById(R.id.etMonthSelector)
        rvRecentExpenses.layoutManager = LinearLayoutManager(this)

        setupMonthSelector()

        findViewById<Button>(R.id.btnQuickAddExpense).setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }

        findViewById<Button>(R.id.btnSetBudget).setOnClickListener {
            showSetBudgetDialog()
        }

        loadDashboardData()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun setupMonthSelector() {
        etMonthSelector.setText(selectedMonth)
        etMonthSelector.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, _ ->
                    val monthStr = (month + 1).toString().padStart(2, '0')
                    selectedMonth = "$year-$monthStr"
                    etMonthSelector.setText(selectedMonth)
                    loadDashboardData()
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                1
            ).show()
        }
    }

    private fun showSetBudgetDialog() {
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        val budgetKey = "budget_$selectedMonth"
        val currentBudget = prefs.getFloat(budgetKey, 0f)

        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "e.g. 5000"
            if (currentBudget > 0f) setText(currentBudget.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Set Budget for $selectedMonth")
            .setMessage("Enter budget for this month")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val budgetString = input.text.toString()
                if (budgetString.isNotEmpty()) {
                    val budget = budgetString.toFloat()
                    prefs.edit().putFloat(budgetKey, budget).apply()
                    loadDashboardData()
                    Toast.makeText(this, "Budget saved for $selectedMonth!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val monthStart = "$selectedMonth-01"
                val monthEnd = getMonthEndDate(selectedMonth)

                val expenses = db.expenseDao().getExpensesByDateRange(monthStart, monthEnd)
                val incomes = db.incomeDao().getAllIncome()

                val filteredIncomes = incomes.filter {
                    it.date >= monthStart && it.date <= monthEnd
                }
                val totalIncome = filteredIncomes.sumOf { it.amount }
                val totalExpenses = expenses.sumOf { it.amount }
                val count = expenses.size + filteredIncomes.size

                val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
                val budgetKey = "budget_$selectedMonth"
                val savedBudget = prefs.getFloat(budgetKey, 0f).toDouble()
                val goal = db.goalDao().getFeaturedGoal()
                val budget = if (savedBudget > 0) savedBudget else (goal?.targetAmount ?: 0.0)
                val remaining = budget - totalExpenses
                val progress = if (budget > 0) ((totalExpenses / budget) * 100).toInt().coerceAtMost(100) else 0

                val allTransactions: List<Any> = expenses + filteredIncomes
                val sortedTransactions = allTransactions.sortedByDescending { transaction: Any ->
                    when (transaction) {
                        is Expense -> transaction.date
                        is Income -> transaction.date
                        else -> ""
                    }
                }

                runOnUiThread {
                    findViewById<TextView>(R.id.tvTotalBudget)?.text = String.format("R %.2f", budget)
                    findViewById<TextView>(R.id.tvTotalSpent)?.text = String.format("R %.2f", totalExpenses)
                    findViewById<TextView>(R.id.tvRemaining)?.text = String.format("R %.2f", remaining)
                    findViewById<TextView>(R.id.tvTransactionCount)?.text = "Transactions: $count"

                    val latestText = sortedTransactions.lastOrNull()?.let { transaction ->
                        when (transaction) {
                            is Expense -> "Expense: ${transaction.description} - R ${String.format("%.2f", transaction.amount)}"
                            is Income -> "Income: ${transaction.description ?: "Income"} - R ${String.format("%.2f", transaction.amount)}"
                            else -> "No transactions yet"
                        }
                    } ?: "No transactions yet"
                    findViewById<TextView>(R.id.tvLatestExpense)?.text = latestText

                    val progressBar = findViewById<ProgressBar>(R.id.progressBudget)
                    progressBar?.progress = progress

                    rvRecentExpenses.adapter = RecentTransactionsAdapter(sortedTransactions.takeLast(5))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getMonthEndDate(monthStr: String): String {
        val c = Calendar.getInstance()
        val parts = monthStr.split("-")
        c.set(Calendar.YEAR, parts[0].toInt())
        c.set(Calendar.MONTH, parts[1].toInt() - 1)
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
    }
}