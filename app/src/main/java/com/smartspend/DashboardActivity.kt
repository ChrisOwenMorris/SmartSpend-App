package com.smartspend

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private lateinit var rvRecentExpenses: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        NavigationHelper.setupMenu(this)

        rvRecentExpenses = findViewById(R.id.rvRecentExpenses)
        rvRecentExpenses.layoutManager = LinearLayoutManager(this)

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

    private fun showSetBudgetDialog() {
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        val currentBudget = prefs.getFloat("monthly_budget", 0f)

        val input = EditText(this).apply {
            this.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            this.hint = "e.g. 5000"
            if (currentBudget > 0f) {
                this.setText(currentBudget.toString())
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Set Monthly Budget")
            .setMessage("Enter your budget for the month")
            .setView(input)
            .setPositiveButton("Save") { dialog, _ ->
                val budgetString = input.text.toString()
                if (budgetString.isNotEmpty()) {
                    val budget = budgetString.toFloat()
                    prefs.edit().putFloat("monthly_budget", budget).apply()
                    loadDashboardData()
                    Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val expenses = db.expenseDao().getAllExpenses()
                val total = expenses.sumOf { it.amount }
                val count = expenses.size
                val latest = expenses.lastOrNull()

                val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
                val savedBudget = prefs.getFloat("monthly_budget", 0f).toDouble()
                val goal = db.goalDao().getFeaturedGoal()
                val budget = if (savedBudget > 0) savedBudget else (goal?.targetAmount ?: 0.0)
                val remaining = budget - total
                val progress = if (budget > 0) ((total / budget) * 100).toInt().coerceAtMost(100) else 0

                runOnUiThread {
                    val tvTotalBudget = findViewById<TextView>(R.id.tvTotalBudget)
                    tvTotalBudget?.text = "R %.2f".format(budget)

                    val tvTotalSpent = findViewById<TextView>(R.id.tvTotalSpent)
                    tvTotalSpent?.text = "R %.2f".format(total)

                    val tvRemaining = findViewById<TextView>(R.id.tvRemaining)
                    tvRemaining?.text = "R %.2f".format(remaining)

                    val tvTransactionCount = findViewById<TextView>(R.id.tvTransactionCount)
                    tvTransactionCount?.text = "Transactions: $count"

                    val tvLatestExpense = findViewById<TextView>(R.id.tvLatestExpense)
                    tvLatestExpense?.text = latest?.let {
                        "Latest: ${it.description} - R %.2f".format(it.amount)
                    } ?: "No expenses yet"

                    val progressBudget = findViewById<ProgressBar>(R.id.progressBudget)
                    progressBudget?.progress = progress

                    rvRecentExpenses.adapter = RecentExpensesAdapter(expenses.takeLast(5).reversed())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}