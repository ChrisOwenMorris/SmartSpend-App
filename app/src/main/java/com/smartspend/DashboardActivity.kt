package com.smartspend

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.Toast

class DashboardActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        NavigationHelper.setupMenu(this)

        // Setup RecyclerView with empty adapter to prevent crash
        val rvRecentExpenses = findViewById<RecyclerView>(R.id.rvRecentExpenses)
        rvRecentExpenses.layoutManager = LinearLayoutManager(this)

        // Quick add expense button
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

    // --- FIXED: Added the missing dialog function ---
    private fun showSetBudgetDialog() {
        val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
        val currentBudget = prefs.getFloat("monthly_budget", 0f)

        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "e.g. 5000"
            // If a budget already exists, show it in the field
            if (currentBudget > 0f) setText(currentBudget.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Set Monthly Budget")
            .setMessage("Enter your budget for the month (This will sync with your dashboard)")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val budgetString = input.text.toString()
                if (budgetString.isNotEmpty()) {
                    val budget = budgetString.toFloat()
                    prefs.edit().putFloat("monthly_budget", budget).apply()

                    // Refresh data after saving
                    loadDashboardData()
                    Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val expenses = db.expenseDao().getAllExpenses()
                val total = expenses.sumOf { it.amount }
                val count = expenses.size
                val latest = expenses.lastOrNull()

                // Logic maintained: Checking shared prefs first, then falling back to Goal
                val prefs = getSharedPreferences("SmartSpendPrefs", MODE_PRIVATE)
                val savedBudget = prefs.getFloat("monthly_budget", 0f).toDouble()

                val goal = db.goalDao().getFeaturedGoal()
                val budget = if (savedBudget > 0) savedBudget else (goal?.targetAmount ?: 0.0)

                val remaining = budget - total
                val progress = if (budget > 0) ((total / budget) * 100).toInt() else 0

                runOnUiThread {
                    findViewById<TextView>(R.id.tvTotalBudget)?.text =
                        "R %.2f".format(budget)

                    findViewById<TextView>(R.id.tvTotalSpent)?.text =
                        "R %.2f".format(total)

                    findViewById<TextView>(R.id.tvRemaining)?.text =
                        "R %.2f".format(remaining)

                    findViewById<TextView>(R.id.tvTransactionCount)?.text =
                        "Transactions: $count"

                    findViewById<TextView>(R.id.tvLatestExpense)?.text =
                        latest?.let {
                            "Latest: ${it.description} - R %.2f".format(it.amount)
                        } ?: "No expenses yet"

                    findViewById<ProgressBar>(R.id.progressBudget)?.progress = progress

                    // Set up recent expenses list
                    val rvRecentExpenses = findViewById<RecyclerView>(R.id.rvRecentExpenses)
                    rvRecentExpenses.adapter = RecentExpensesAdapter(expenses.takeLast(5).reversed())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}