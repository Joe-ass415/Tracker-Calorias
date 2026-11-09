package com.calorietracker.mobile.presentation.viewmodel

import com.calorietracker.mobile.data.local.entity.MealEntity
import com.calorietracker.shared.model.DailyBalancePayload
import com.calorietracker.shared.model.MealType

data class MobileDashboardUiState(
    val meals: List<MealEntity> = emptyList(),
    val targetCalories: Int = 2000,
    val dailyBalance: DailyBalancePayload = DailyBalancePayload(
        consumedCalories = 0,
        targetCalories = 2000,
        remainingCalories = 2000,
        isLimitExceeded = false
    ),
    val isProcessingAi: Boolean = false,
    val userNotification: String? = null
) {
    val mealsByType: Map<MealType, List<MealEntity>>
        get() = meals.groupBy { it.toDomainMealType() }

    val totalProteinG: Double
        get() = meals.sumOf { it.proteinG }

    val totalCarbsG: Double
        get() = meals.sumOf { it.carbsG }

    val totalFatG: Double
        get() = meals.sumOf { it.fatG }
}

