package com.smartspend

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import java.text.SimpleDateFormat
import java.util.*

class ExpenseActivity : AppCompatActivity() {

    private lateinit var amountInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var summaryText: TextView
    private lateinit var summaryDate: TextView
    private lateinit var toggleExpense: Button
    private lateinit var toggleIncome: Button

    private var isExpense = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        amountInput = findViewById(R.id.etAmount)
        dateInput = findViewById(R.id.etDate)
        summaryText = findViewById(R.id.tvSummaryAmount)
        summaryDate = findViewById(R.id.tvSummaryDate)
        toggleExpense = findViewById(R.id.btnExpense)
        toggleIncome = findViewById(R.id.btnIncome)

        val calendar = Calendar.getInstance()
        updateDate(calendar)

        dateInput.setOnClickListener {
            DatePickerDialog(this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    updateDate(calendar)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        toggleExpense.setOnClickListener {
            isExpense = true
            updateSummary()
        }

        toggleIncome.setOnClickListener {
            isExpense = false
            updateSummary()
        }

        amountInput.addTextChangedListener {
            updateSummary()
        }
    }

    private fun updateDate(calendar: Calendar) {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formatted = format.format(calendar.time)

        dateInput.setText(formatted)
        summaryDate.text = formatted
    }

    private fun updateSummary() {
        val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0

        val display = if (isExpense) "-$%.2f".format(amount)
        else "+$%.2f".format(amount)

        summaryText.text = display

        if (isExpense) {
            summaryText.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            summaryText.setTextColor(getColor(android.R.color.holo_green_dark))
        }
    }
}