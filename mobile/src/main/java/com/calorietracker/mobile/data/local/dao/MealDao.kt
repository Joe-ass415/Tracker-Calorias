package com.calorietracker.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calorietracker.mobile.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Query("SELECT * FROM meals WHERE timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp DESC")
    fun getMealsForDay(startOfDay: Long, endOfDay: Long): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp DESC")
    suspend fun getMealsForDayDirect(startOfDay: Long, endOfDay: Long): List<MealEntity>

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMeal(): MealEntity?

    @Query("SELECT SUM(calories) FROM meals WHERE timestamp BETWEEN :startOfDay AND :endOfDay")
    fun getTodayCalories(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Query("DELETE FROM meals")
    suspend fun clearAll()
}

