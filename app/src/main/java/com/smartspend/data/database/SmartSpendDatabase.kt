package com.smartspend.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smartspend.data.dao.ExpenseDao
import com.smartspend.data.dao.UserDao
import com.smartspend.data.dao.GoalDao
import com.smartspend.data.dao.IncomeDao
import com.smartspend.data.entity.Category
import com.smartspend.data.entity.Expense
import com.smartspend.data.entity.Goal
import com.smartspend.data.entity.User
import com.smartspend.data.entity.Income

// Fixed the syntax error in the annotation below
@Database(
    entities = [Expense::class, Category::class, Income::class, User::class, Goal::class],
    version = 2,
    exportSchema = false
)
abstract class SmartSpendDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun userDao(): UserDao
    abstract fun goalDao(): GoalDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        @Volatile
        private var INSTANCE: SmartSpendDatabase? = null

        fun getDatabase(context: Context): SmartSpendDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartSpendDatabase::class.java,
                    "smartspend_db"
                )
                    .fallbackToDestructiveMigration() // Useful during development when changing schema
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}