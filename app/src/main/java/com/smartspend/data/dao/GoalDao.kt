package com.smartspend.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.smartspend.data.entity.Goal

@Dao
interface GoalDao {

    @Insert
    suspend fun insert(goal: Goal)

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("SELECT * FROM goals ORDER BY goalId ASC")
    suspend fun getAllGoals(): List<Goal>

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY goalId ASC LIMIT 1")
    suspend fun getFeaturedGoal(): Goal?

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY goalId ASC")
    suspend fun getActiveGoals(): List<Goal>

    @Query("UPDATE goals SET currentAmount = :amount WHERE goalId = :goalId")
    suspend fun updateCurrentAmount(goalId: Int, amount: Double)

    @Query("UPDATE goals SET isCompleted = 1 WHERE goalId = :goalId")
    suspend fun markAsCompleted(goalId: Int)
}