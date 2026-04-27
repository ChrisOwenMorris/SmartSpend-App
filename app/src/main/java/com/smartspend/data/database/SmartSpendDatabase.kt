package com.smartspend.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smartspend.data.dao.ExpenseDao
import com.smartspend.data.entity.Category
import com.smartspend.data.entity.Expense
import com.smartspend.data.entity.Goal
import com.smartspend.data.entity.User

@Database(
    entities = [User::class, Category::class, Expense::class, Goal::class],
    version = 1,
    exportSchema = false
)
abstract class SmartSpendDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: SmartSpendDatabase? = null

        fun getDatabase(context: Context): SmartSpendDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartSpendDatabase::class.java,
                    "smartspend_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}