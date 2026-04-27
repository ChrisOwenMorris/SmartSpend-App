package com.smartspend

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        NavigationHelper.setupMenu(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavigationHelper.goToDashboard(this@SettingsActivity)
            }
        })

        // --- SHARED PREFERENCES --- //
        prefs = getSharedPreferences("smartspend_prefs", Context.MODE_PRIVATE)

        // --- VIEWS --- //
        val switchTheme = findViewById<SwitchMaterial>(R.id.switchTheme)
        val switchBiometric = findViewById<SwitchMaterial>(R.id.switchBiometric)
        val switchSpendingAlerts = findViewById<SwitchMaterial>(R.id.switchSpendingAlerts)
        val switchReminders = findViewById<SwitchMaterial>(R.id.switchReminders)
        val switchGoalUpdates = findViewById<SwitchMaterial>(R.id.switchGoalUpdates)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val btnSaveSettings = findViewById<Button>(R.id.btnSaveSettings)

        // --- LOAD SAVED PREFERENCES --- //
        switchTheme.isChecked = prefs.getBoolean("dark_mode", false)
        switchBiometric.isChecked = prefs.getBoolean("biometric_enabled", true)
        switchSpendingAlerts.isChecked = prefs.getBoolean("spending_alerts", true)
        switchReminders.isChecked = prefs.getBoolean("reminders", false)
        switchGoalUpdates.isChecked = prefs.getBoolean("goal_updates", true)

        // --- THEME SWITCH --- //
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // --- CHANGE PASSWORD --- //
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // --- SAVE SETTINGS --- //
        btnSaveSettings.setOnClickListener {
            prefs.edit()
                .putBoolean("dark_mode", switchTheme.isChecked)
                .putBoolean("biometric_enabled", switchBiometric.isChecked)
                .putBoolean("spending_alerts", switchSpendingAlerts.isChecked)
                .putBoolean("reminders", switchReminders.isChecked)
                .putBoolean("goal_updates", switchGoalUpdates.isChecked)
                .apply()

            // Send test notifications for enabled ones
            if (switchSpendingAlerts.isChecked) {
                sendNotification(
                    "Spending Alert",
                    "You are being notified about your spending activity.",
                    1
                )
            }
            if (switchReminders.isChecked) {
                sendNotification(
                    "Bill Reminder",
                    "You have upcoming bill and payment reminders.",
                    2
                )
            }
            if (switchGoalUpdates.isChecked) {
                sendNotification(
                    "Goal Update",
                    "Check your progress on your savings goals.",
                    3
                )
            }

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }
    }

    // --- CHANGE PASSWORD DIALOG --- //
    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val current = etCurrentPassword.text.toString()
                val new = etNewPassword.text.toString()
                val confirm = etConfirmPassword.text.toString()

                when {
                    current.isEmpty() || new.isEmpty() || confirm.isEmpty() -> {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                    new != confirm -> {
                        Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                    new.length < 6 -> {
                        Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        updatePassword(current, new)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // --- UPDATE PASSWORD IN DATABASE --- //
    private fun updatePassword(currentPassword: String, newPassword: String) {
        val loggedInEmail = prefs.getString("logged_in_email", "") ?: ""

        lifecycleScope.launch {
            val user = db.userDao().getUserByEmailAndPassword(loggedInEmail, currentPassword)
            if (user != null) {
                db.userDao().updatePassword(user.userId, newPassword)
                runOnUiThread {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Password updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Current password is incorrect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // --- SEND NOTIFICATION --- //
    private fun sendNotification(title: String, message: String, notificationId: Int) {
        val channelId = "smartspend_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SmartSpend Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        if (!areNotificationsEnabled()) {
            Toast.makeText(this, "Please enable notifications for SmartSpend in settings", Toast.LENGTH_LONG).show()
            return
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    // --- CHECK IF NOTIFICATIONS ARE ENABLED --- //
    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }
}