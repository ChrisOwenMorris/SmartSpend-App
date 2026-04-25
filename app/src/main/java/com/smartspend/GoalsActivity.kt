package com.smartspend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class GoalsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This links the Kotlin logic to your activity_goals.xml layout
        setContentView(R.layout.activity_goals)
    }
}