package com.smartspend.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IncomeDao {

    @Insert
    suspend fun insert(income: com.smartspend.data.entity.Income)

    @Query("SELECT * FROM income ORDER BY date DESC")
    suspend fun getAllIncome(): List<com.smartspend.data.entity.Income>

    @Delete
    suspend fun delete(income: com.smartspend.data.entity.Income)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM income WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalIncomeByDateRange(startDate: String, endDate: String): Double

    @Query("SELECT source, SUM(amount) as total FROM income WHERE date BETWEEN :startDate AND :endDate GROUP BY source")
    suspend fun getIncomeBySource(startDate: String, endDate: String): List<SourceSummary>
}

data class SourceSummary(
    val source: String,
    val total: Double
)