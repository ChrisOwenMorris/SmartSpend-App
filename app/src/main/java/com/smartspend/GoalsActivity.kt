package com.smartspend

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.smartspend.data.entity.Goal
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.core.net.toUri

class GoalsActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private var selectedImageUri: Uri? = null
    private var featuredGoal: Goal? = null
    private lateinit var goalsAdapter: GoalsAdapter

    // Image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            findViewById<ImageView>(R.id.ivNewGoalImage).setImageURI(it)
            findViewById<TextView>(R.id.tvUploadImage).text = getString(R.string.image_selected)
        }
    }

    // Featured image picker
    private val featuredImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            findViewById<ImageView>(R.id.ivFeaturedGoalImage).setImageURI(it)
            featuredGoal?.let { goal ->
                lifecycleScope.launch {
                    db.goalDao().update(goal.copy(imagePath = it.toString()))
                    loadFeaturedGoal()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        NavigationHelper.setupMenu(this)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavigationHelper.goToDashboard(this@GoalsActivity)
            }
        })

        setupRecyclerView()
        setupButtons()
        loadAllGoals()
        loadFeaturedGoal()
    }

    private fun setupRecyclerView() {
        goalsAdapter = GoalsAdapter(emptyList()) { goal ->
            showUpdateGoalDialog(goal)
        }
        findViewById<RecyclerView>(R.id.rvAllGoals).apply {
            layoutManager = LinearLayoutManager(this@GoalsActivity)
            adapter = goalsAdapter
        }
    }

    private fun setupButtons() {

        // Featured image tap
        findViewById<FrameLayout>(R.id.frameFeaturedImage).setOnClickListener {
            featuredImagePickerLauncher.launch("image/*")
        }

        // Image upload for new goal
        findViewById<LinearLayout>(R.id.layoutUploadImage).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Target date picker
        findViewById<TextInputEditText>(R.id.etTargetDate).setOnClickListener {
            showDatePicker { date ->
                findViewById<TextInputEditText>(R.id.etTargetDate).setText(date)
            }
        }

        // Create goal button
        findViewById<MaterialButton>(R.id.btnCreateGoal).setOnClickListener {
            createGoal()
        }

        // Add savings button
        findViewById<MaterialButton>(R.id.btnAddSavings).setOnClickListener {
            showAddSavingsDialog()
        }

        // Update goal button
        findViewById<MaterialButton>(R.id.btnUpdateGoal).setOnClickListener {
            showSelectGoalToUpdateDialog()
        }
    }

    private fun loadFeaturedGoal() {
        lifecycleScope.launch {
            val goal = db.goalDao().getFeaturedGoal()
            featuredGoal = goal

            if (goal != null) {
                val percentage = if (goal.targetAmount > 0)
                    ((goal.currentAmount / goal.targetAmount) * 100).toFloat()
                else 0f

                // Check if goal is completed
                if (percentage >= 100f) {
                    db.goalDao().markAsCompleted(goal.goalId)
                    showCongratulationsCard()
                    loadFeaturedGoal()
                    return@launch
                }

                runOnUiThread {
                    findViewById<TextView>(R.id.tvFeaturedGoalTitle).text = goal.goalName
                    findViewById<TextView>(R.id.tvFeaturedGoalDate).text =
                        getString(R.string.target_date_format, goal.targetDate)
                    findViewById<TextView>(R.id.tvFeaturedCurrentAmount).text =
                        getString(R.string.amount_format, goal.currentAmount)
                    findViewById<TextView>(R.id.tvFeaturedGoalAmount).text =
                        getString(R.string.amount_format, goal.targetAmount)
                    findViewById<TextView>(R.id.tvFeaturedGoalPercentage).text =
                        getString(R.string.percentage_format, percentage.toInt())
                    findViewById<ProgressBar>(R.id.progressFeaturedGoal).progress =
                        percentage.toInt()
                    findViewById<PieChartView>(R.id.pieChartFeatured).setPercentage(percentage)

                    goal.imagePath?.let { path ->
                        findViewById<ImageView>(R.id.ivFeaturedGoalImage)
                            .setImageURI(path.toUri())
                    }
                }
            } else {
                runOnUiThread {
                    findViewById<TextView>(R.id.tvFeaturedGoalTitle).text =
                        getString(R.string.no_goals_yet)
                    findViewById<TextView>(R.id.tvFeaturedGoalDate).text = ""
                    findViewById<TextView>(R.id.tvFeaturedCurrentAmount).setText(R.string.amount_zero)
                    findViewById<TextView>(R.id.tvFeaturedGoalAmount).setText(R.string.amount_zero)
                    findViewById<TextView>(R.id.tvFeaturedGoalPercentage).setText(R.string.percentage_zero)
                    findViewById<ProgressBar>(R.id.progressFeaturedGoal).progress = 0
                    findViewById<PieChartView>(R.id.pieChartFeatured).setPercentage(0f)
                }
            }
        }
    }

    private fun loadAllGoals() {
        lifecycleScope.launch {
            val goals = db.goalDao().getActiveGoals()
            runOnUiThread {
                goalsAdapter.updateGoals(goals)
            }
        }
    }

    private fun createGoal() {
        val name = findViewById<TextInputEditText>(R.id.etGoalName).text.toString().trim()
        val amountStr = findViewById<TextInputEditText>(R.id.etGoalAmount).text.toString().trim()
        val date = findViewById<TextInputEditText>(R.id.etTargetDate).text.toString().trim()

        when {
            name.isEmpty() -> {
                Toast.makeText(this, getString(R.string.enter_goal_name), Toast.LENGTH_SHORT).show()
                return
            }
            amountStr.isEmpty() -> {
                Toast.makeText(this, getString(R.string.enter_goal_amount), Toast.LENGTH_SHORT).show()
                return
            }
            date.isEmpty() -> {
                Toast.makeText(this, getString(R.string.enter_target_date), Toast.LENGTH_SHORT).show()
                return
            }
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            db.goalDao().insert(
                Goal(
                    goalName = name,
                    targetAmount = amount,
                    targetDate = date,
                    imagePath = selectedImageUri?.toString()
                )
            )
            runOnUiThread {
                Toast.makeText(
                    this@GoalsActivity,
                    getString(R.string.goal_created),
                    Toast.LENGTH_SHORT
                ).show()
                // Clear form
                findViewById<TextInputEditText>(R.id.etGoalName).text?.clear()
                findViewById<TextInputEditText>(R.id.etGoalAmount).text?.clear()
                findViewById<TextInputEditText>(R.id.etTargetDate).text?.clear()
                findViewById<ImageView>(R.id.ivNewGoalImage)
                    .setImageResource(android.R.drawable.ic_menu_camera)
                findViewById<TextView>(R.id.tvUploadImage).text =
                    getString(R.string.upload_goal_image)
                selectedImageUri = null
            }
            loadAllGoals()
            loadFeaturedGoal()
        }
    }

    private fun showAddSavingsDialog() {
        val goal = featuredGoal ?: run {
            Toast.makeText(this, getString(R.string.no_goals_yet), Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(this).apply {
            hint = getString(R.string.enter_amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_savings_to, goal.goalName))
            .setView(input)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    lifecycleScope.launch {
                        val newAmount = goal.currentAmount + amount
                        db.goalDao().updateCurrentAmount(goal.goalId, newAmount)

                        // Also add as expense
                        db.expenseDao().insert(
                            com.smartspend.data.entity.Expense(
                                amount = amount,
                                description = "Savings: ${goal.goalName}",
                                date = java.time.LocalDate.now().toString(),
                                startTime = "00:00",
                                endTime = "00:00",
                                categoryId = 0,
                                receiptPath = null
                            )
                        )

                        runOnUiThread {
                            Toast.makeText(
                                this@GoalsActivity,
                                getString(R.string.savings_added),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        loadFeaturedGoal()
                        loadAllGoals()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showSelectGoalToUpdateDialog() {
        lifecycleScope.launch {
            val goals = db.goalDao().getActiveGoals()
            if (goals.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(
                        this@GoalsActivity,
                        getString(R.string.no_goals_yet),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            val goalNames = goals.map { it.goalName }.toTypedArray()

            runOnUiThread {
                AlertDialog.Builder(this@GoalsActivity)
                    .setTitle(getString(R.string.select_goal_to_update))
                    .setItems(goalNames) { _, index ->
                        showUpdateGoalDialog(goals[index])
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        }
    }

    private fun showUpdateGoalDialog(goal: Goal) {
        val input = EditText(this).apply {
            hint = getString(R.string.enter_amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_goal_amount, goal.goalName))
            .setMessage(
                getString(
                    R.string.current_saved_of,
                    goal.currentAmount,
                    goal.targetAmount
                )
            )
            .setView(input)
            .setPositiveButton(getString(R.string.update)) { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    lifecycleScope.launch {
                        val newAmount = goal.currentAmount + amount
                        db.goalDao().updateCurrentAmount(goal.goalId, newAmount)
                        runOnUiThread {
                            Toast.makeText(
                                this@GoalsActivity,
                                getString(R.string.goal_updated),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        loadFeaturedGoal()
                        loadAllGoals()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showCongratulationsCard() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("🎉 " + getString(R.string.congratulations))
                .setMessage(
                    getString(
                        R.string.goal_completed_message,
                        featuredGoal?.goalName ?: ""
                    )
                )
                .setPositiveButton(getString(R.string.next_goal), null)
                .show()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                onDateSelected("$year-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}