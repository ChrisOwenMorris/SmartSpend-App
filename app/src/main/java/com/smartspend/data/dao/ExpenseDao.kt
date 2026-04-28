package com.smartspend.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.smartspend.data.entity.Expense

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate")
    suspend fun getExpensesByDateRange(startDate: String, endDate: String): List<Expense>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalByDateRange(startDate: String, endDate: String): Double

    @Query("SELECT categoryId, SUM(amount) as total FROM expenses WHERE date >= :startDate AND date <= :endDate GROUP BY categoryId ORDER BY total DESC LIMIT 4")
    suspend fun getTopCategoriesByDateRange(startDate: String, endDate: String): List<CategoryTotal>

    @Query("""
        SELECT c.categoryName, SUM(e.amount) as total 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.categoryId 
        WHERE e.date >= :startDate AND e.date <= :endDate 
        GROUP BY e.categoryId 
        ORDER BY total DESC 
        LIMIT 4
    """)
    suspend fun getTopCategoriesWithNames(startDate: String, endDate: String): List<CategoryWithTotal>
}

data class CategoryTotal(
    val categoryId: Int,
    val total: Double
)

data class CategoryWithTotal(
    val categoryName: String,
    val total: Double
)