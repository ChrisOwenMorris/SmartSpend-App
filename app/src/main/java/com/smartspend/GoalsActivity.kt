package com.smartspend

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartspend.data.database.SmartSpendDatabase
import com.smartspend.data.entity.Goal
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class GoalsActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private lateinit var database: SmartSpendDatabase
    private lateinit var adapter: GoalsAdapter

    private lateinit var rvGoals: androidx.recyclerview.widget.RecyclerView
    private lateinit var sbGoalAmount: SeekBar
    private lateinit var tvSelectedAmount: TextView
    private lateinit var etTargetDate: EditText
    private lateinit var btnSaveGoal: Button
    private lateinit var etGoalName: EditText
    private lateinit var etMinGoal: EditText
    private lateinit var etMaxGoal: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        database = db
        NavigationHelper.setupMenu(this)

        rvGoals = findViewById(R.id.rvGoals)
        sbGoalAmount = findViewById(R.id.sbGoalAmount)
        tvSelectedAmount = findViewById(R.id.tvSelectedAmount)
        etTargetDate = findViewById(R.id.etTargetDate)
        btnSaveGoal = findViewById(R.id.btnSaveGoal)
        etGoalName = findViewById(R.id.etGoalName)
        etMinGoal = findViewById(R.id.etMinGoal)
        etMaxGoal = findViewById(R.id.etMaxGoal)

        setupRecyclerView()
        setupListeners()
        observeGoals()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavigationHelper.goToDashboard(this@GoalsActivity)
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = GoalsAdapter(emptyList()) { goal -> deleteGoal(goal) }
        rvGoals.layoutManager = LinearLayoutManager(this)
        rvGoals.adapter = adapter
    }

    private fun setupListeners() {
        sbGoalAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSelectedAmount.text = "Selected: R$${progress}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        etTargetDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                val monthDisplay = month + 1
                etTargetDate.setText(String.format("%02d-%02d-%04d", monthDisplay, day, year))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        btnSaveGoal.setOnClickListener { saveGoal() }
    }

    private fun saveGoal() {
        val name = etGoalName.text.toString()
        val amount = sbGoalAmount.progress.toDouble()
        val min = etMinGoal.text.toString().toDoubleOrNull() ?: 0.0
        val max = etMaxGoal.text.toString().toDoubleOrNull() ?: 0.0
        val date = etTargetDate.text.toString()

        if (name.isEmpty() || date.isEmpty() || amount <= 0.0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val goal = Goal(
            name = name,
            targetAmount = amount,
            minMonthly = min,
            maxMonthly = max,
            targetDate = date
        )

        lifecycleScope.launch {
            database.goalDao().insertGoal(goal)
            Toast.makeText(this@GoalsActivity, "Goal saved", Toast.LENGTH_SHORT).show()
            clearFields()
        }
    }

    private fun deleteGoal(goal: Goal) {
        lifecycleScope.launch {
            database.goalDao().deleteGoal(goal)
            Toast.makeText(this@GoalsActivity, "Goal deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeGoals() {
        lifecycleScope.launch {
            database.goalDao().getAllGoals().collect { goals ->
                adapter.updateGoals(goals)
            }
        }
    }

    private fun clearFields() {
        etGoalName.text.clear()
        sbGoalAmount.progress = 0
        etMinGoal.text.clear()
        etMaxGoal.text.clear()
        etTargetDate.text.clear()
        tvSelectedAmount.text = "Selected: R$0"
    }
}
