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

        loadDashboardData()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val expenses = db.expenseDao().getAllExpenses()
                val total = expenses.sumOf { it.amount }
                val count = expenses.size
                val latest = expenses.lastOrNull()

                // Load goal for budget calculation
                val goal = db.goalDao().getFeaturedGoal()
                val budget = goal?.targetAmount ?: 0.0
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