package com.smartspend

import android.app.Application
import com.smartspend.data.database.SmartSpendDatabase

class SmartSpendApp : Application() {
    val database: SmartSpendDatabase by lazy {
        SmartSpendDatabase.getDatabase(this)
    }
}