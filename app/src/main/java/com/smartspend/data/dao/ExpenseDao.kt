package com.smartspend.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.smartspend.data.entity.Expense

@Dao
interface ExpenseDao {

    // 🔹 INSERT EXPENSE
    @Insert
    suspend fun insert(expense: Expense)

    // 🔹 GET ALL EXPENSES
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpenses(): List<Expense>

    // 🔹 DELETE EXPENSE
    @Delete
    suspend fun delete(expense: Expense)

    // 🔹 GET EXPENSES BY DATE RANGE
    @Query("""
        SELECT * FROM expenses 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date DESC
    """)
    suspend fun getExpensesByDateRange(startDate: String, endDate: String): List<Expense>

    // 🔹 TOTAL AMOUNT IN DATE RANGE
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM expenses 
        WHERE date >= :startDate AND date <= :endDate
    """)
    suspend fun getTotalByDateRange(startDate: String, endDate: String): Double

    // 🔹 TOP CATEGORIES (BY ID)
    @Query("""
        SELECT categoryId, COALESCE(SUM(amount), 0.0) as total 
        FROM expenses 
        WHERE date >= :startDate AND date <= :endDate 
        GROUP BY categoryId 
        ORDER BY total DESC 
        LIMIT 4
    """)
    suspend fun getTopCategoriesByDateRange(
        startDate: String,
        endDate: String
    ): List<CategoryTotal>

    // 🔹 TOP CATEGORIES WITH NAMES (JOIN)
    @Query("""
        SELECT c.categoryName, COALESCE(SUM(e.amount), 0.0) as total 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.categoryId 
        WHERE e.date >= :startDate AND e.date <= :endDate 
        GROUP BY e.categoryId 
        ORDER BY total DESC 
        LIMIT 4
    """)
    suspend fun getTopCategoriesWithNames(
        startDate: String,
        endDate: String
    ): List<CategoryWithTotal>

    // 🔹 PIE CHART DATA (CATEGORY BREAKDOWN)
    @Query("""
        SELECT c.categoryName, COALESCE(SUM(e.amount), 0.0) as total 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.categoryId 
        WHERE e.date BETWEEN :startDate AND :endDate 
        GROUP BY c.categoryName
    """)
    suspend fun getExpensesGroupedByCategory(
        startDate: String,
        endDate: String
    ): List<CategorySummary>

    // 🔥🔥🔥 FIXED MONTHLY TRENDS (NO MORE CRASH)
    @Query("""
        SELECT 
            COALESCE(strftime('%m', date), '00') AS month,
            COALESCE(SUM(amount), 0.0) AS total
        FROM expenses
        WHERE date >= :sixMonthsAgo
        GROUP BY strftime('%m', date)
        ORDER BY strftime('%m', date) ASC
    """)
    suspend fun getMonthlyTrends(sixMonthsAgo: String): List<TrendSummary>
}

//////////////////////////////////////////////////////////////////
// 🔹 DATA CLASSES
//////////////////////////////////////////////////////////////////

data class CategoryTotal(
    val categoryId: Int,
    val total: Double
)

data class CategoryWithTotal(
    val categoryName: String,
    val total: Double
)

data class CategorySummary(
    val categoryName: String,
    val total: Double
)

data class TrendSummary(
    val month: String,
    val total: Double
)