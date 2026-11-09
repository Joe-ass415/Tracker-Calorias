package com.calorietracker.mobile.data.repository

import com.calorietracker.mobile.data.local.entity.MealEntity
import com.calorietracker.shared.model.DailyBalancePayload
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun getTodayMeals(): Flow<List<MealEntity>>
    fun getTargetCalories(): Flow<Int>
    fun getDailyBalanceFlow(): Flow<DailyBalancePayload>

    suspend fun processAndRecordMeal(voiceText: String, requestId: String = ""): Result<DailyBalancePayload>
    suspend fun calculateCurrentBalance(): DailyBalancePayload
    suspend fun updateTargetCalories(calories: Int)
    suspend fun deleteMeal(mealId: Long)
    suspend fun clearHistory()
}

