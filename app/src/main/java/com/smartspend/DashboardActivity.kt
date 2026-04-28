package com.smartspend

import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        NavigationHelper.setupMenu(this)

        loadDashboardData()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    private fun loadDashboardData() {

        lifecycleScope.launch {

            val expenses = db.expenseDao().getAllExpenses()

            // Total spent
            val total = expenses.sumOf { it.amount }

            // Number of transactions
            val count = expenses.size

            // Latest expense
            val latest = expenses.lastOrNull()

            runOnUiThread {

                findViewById<TextView>(R.id.tvTotalSpent)?.text =
                    "Total Spent: R$total"

                findViewById<TextView>(R.id.tvTransactionCount)?.text =
                    "Transactions: $count"

                findViewById<TextView>(R.id.tvLatestExpense)?.text =
                    latest?.let {
                        "Latest: ${it.description} - R${it.amount}"
                    } ?: "No expenses yet"
            }
        }
    }
}