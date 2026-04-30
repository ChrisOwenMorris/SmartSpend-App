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
import com.smartspend.data.database.SmartSpendDatabase

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

    private var receiptPath: String? = null
    private var isExpense = true
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        NavigationHelper.setupMenu(this)

        receiptPath = intent.getStringExtra("receiptPath")

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
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupListeners() {
        btnExpense.setOnClickListener {
            isExpense = true
            updateSummary()
        }

        btnIncome.setOnClickListener {
            isExpense = false
            updateSummary()
        }

        etAmount.addTextChangedListener {
            updateSummary()
        }

        val quickButtons = listOf(
            "Groceries" to 1200.0,
            "Gas" to 700.0,
            "Coffee" to 35.0
        )

        val buttonBar = findViewById<LinearLayout>(R.id.quick_add_container)
        if (buttonBar != null) {
            for (i in 0 until buttonBar.childCount) {
                val btn = buttonBar.getChildAt(i) as? Button ?: continue
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
        lifecycleScope.launch {
            val categories = db.expenseDao().getAllExpenses().map { it.categoryId }.distinct().sorted()
            val categoryNames = listOf("Food", "Transport", "Shopping", "Bills", "Other")
            val adapter = ArrayAdapter(
                this@ExpenseActivity,
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            runOnUiThread {
                spCategory.adapter = adapter
            }
        }
    }

    private fun saveExpense() {
        val amountStr = etAmount.text.toString()
        val description = etDescription.text.toString()
        val date = etDate.text.toString()

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryId = spCategory.selectedItemPosition + 1
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        lifecycleScope.launch {
            try {
                db.expenseDao().insert(
                    Expense(
                        amount = amount,
                        description = description,
                        date = dateFormatted,
                        startTime = "00:00",
                        endTime = "23:59",
                        categoryId = categoryId,
                        receiptPath = receiptPath
                    )
                )

                runOnUiThread {
                    Toast.makeText(this@ExpenseActivity, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
                    clearForm()
                    NavigationHelper.goToDashboard(this@ExpenseActivity)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ExpenseActivity, "Error saving expense", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearForm() {
        etAmount.text.clear()
        etDescription.text.clear()
        spCategory.setSelection(0)
        updateSummary()
    }
}