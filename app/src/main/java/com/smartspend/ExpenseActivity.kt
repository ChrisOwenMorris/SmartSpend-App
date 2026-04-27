package com.smartspend

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.smartspend.data.entity.Expense



class ExpenseActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvSummaryAmount: TextView
    private lateinit var tvSummaryDate: TextView
    private lateinit var btnExpense: Button
    private lateinit var btnIncome: Button
    private lateinit var spCategory: Spinner

    private lateinit var btnSave: Button

    private var isExpense = true
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        NavigationHelper.setupMenu(this)

        bindViews()
        setupDatePicker()
        setupListeners()
        setupCategorySpinner()
        updateDate()
        updateSummary()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavigationHelper.goToDashboard(this@ExpenseActivity)
            }
        })
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            saveExpense()
        }
    }

    private fun bindViews() {
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        tvSummaryAmount = findViewById(R.id.tvSummaryAmount)
        tvSummaryDate = findViewById(R.id.tvSummaryDate)
        btnExpense = findViewById(R.id.btnExpense)
        btnIncome = findViewById(R.id.btnIncome)
        spCategory = findViewById(R.id.spCategory)
    }

    private fun setupListeners() {

        // Toggle buttons
        btnExpense.setOnClickListener {
            isExpense = true
            updateSummary()
        }

        btnIncome.setOnClickListener {
            isExpense = false
            updateSummary()
        }

        // Amount live update
        etAmount.addTextChangedListener {
            updateSummary()
        }

        // Quick Add Buttons
        val quickButtons = listOf(
            "Groceries" to 1200.0,
            "Gas" to 700.0,
            "Coffee" to 35.0
        )

        val buttonBar = findViewById<LinearLayout>(R.id.quick_add_container)
        if (buttonBar != null) {
            for (i in 0 until buttonBar.childCount) {
                val btn = buttonBar.getChildAt(i) as Button
                val value = quickButtons.getOrNull(i)?.second ?: 0.0
                btn.setOnClickListener {
                    etAmount.setText(value.toString())
                }
            }
        }
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    updateDate()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDate() {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formatted = format.format(calendar.time)
        etDate.setText(formatted)
        tvSummaryDate.text = formatted
    }

    private fun updateSummary() {
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val display = if (isExpense) "-R%.2f".format(amount) else "+R%.2f".format(amount)

        tvSummaryAmount.text = display
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Food", "Transport", "Shopping", "Bills", "Other")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = adapter
    }

    // 🚀 SAVE FUNCTION (CAN BE CALLED LATER FROM BUTTON)
    private fun saveExpense() {

        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val description = etDescription.text.toString()
        val date = etDate.text.toString()
        val categoryId = spCategory.selectedItemPosition + 1

        lifecycleScope.launch {
            db.expenseDao().insert(
                Expense(
                    amount = amount,
                    description = description,
                    date = date,
                    startTime = "00:00",
                    endTime = "00:00",
                    categoryId = categoryId,
                    receiptPath = null
                )
            )

            Toast.makeText(
                this@ExpenseActivity,
                "Expense Saved!",
                Toast.LENGTH_SHORT
            ).show()

            clearForm()
        }
    }

    private fun clearForm() {
        etAmount.text.clear()
        etDescription.text.clear()
        updateSummary()
    }
}