package com.calorietracker.wear.presentation.viewmodel

import com.calorietracker.shared.model.DailyBalancePayload

data class MealTrackerUiState(
    val balance: DailyBalancePayload = DailyBalancePayload(
        consumedCalories = 0,
        targetCalories = 2000,
        remainingCalories = 2000,
        isLimitExceeded = false
    ),
    val pendingOfflineCount: Int = 0,
    val isPhoneConnected: Boolean = true,
    val isSending: Boolean = false,
    val statusMessage: String? = null
)

