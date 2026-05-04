package com.smartspend

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.smartspend.data.entity.Income

class IncomeActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvSummaryAmount: TextView
    private lateinit var tvSummaryDate: TextView
    private lateinit var btnSave: Button
    private lateinit var spSource: Spinner
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income)

        NavigationHelper.setupMenu(this)

        bindViews()
        setupDatePicker()
        setupListeners()
        setupSourceSpinner()
        updateDate()
        updateSummary()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavigationHelper.goToDashboard(this@IncomeActivity)
            }
        })

        btnSave.setOnClickListener {
            saveIncome()
        }
    }

    private fun bindViews() {
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        tvSummaryAmount = findViewById(R.id.tvSummaryAmount)
        tvSummaryDate = findViewById(R.id.tvSummaryDate)
        btnSave = findViewById(R.id.btnSave)
        spSource = findViewById(R.id.spSource)
    }

    private fun setupListeners() {
        etAmount.addTextChangedListener {
            updateSummary()
        }

        val quickButtons = listOf(
            "Salary" to 5000.0,
            "Freelance" to 1200.0,
            "Bonus" to 800.0
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

    private fun setupSourceSpinner() {
        val sources = arrayOf("Salary", "Freelance", "Bonus", "Investment", "Gift", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sources)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSource.adapter = adapter
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
        tvSummaryAmount.text = "+R %.2f".format(amount)
    }

    private fun saveIncome() {
        val amountStr = etAmount.text.toString()
        val description = etDescription.text.toString()
        val source = spSource.selectedItem.toString()

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        lifecycleScope.launch {
            try {
                db.incomeDao().insert(
                    Income(
                        source = source,
                        amount = amount,
                        date = dateFormatted,
                        description = description
                    )
                )

                runOnUiThread {
                    Toast.makeText(this@IncomeActivity, "Income saved successfully!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@IncomeActivity, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@IncomeActivity, "Error saving income", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}