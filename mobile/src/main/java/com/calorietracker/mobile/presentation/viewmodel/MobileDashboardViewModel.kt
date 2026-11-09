package com.calorietracker.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calorietracker.mobile.data.repository.MealRepository
import com.calorietracker.mobile.service.WearSyncClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MobileDashboardViewModel(
    private val repository: MealRepository,
    private val wearSyncClient: WearSyncClient
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    private val _userNotification = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MobileDashboardUiState> = combine(
        repository.getTodayMeals(),
        repository.getTargetCalories(),
        repository.getDailyBalanceFlow(),
        _isProcessing,
        _userNotification
    ) { meals, target, balance, isProcessing, notification ->
        MobileDashboardUiState(
            meals = meals,
            targetCalories = target,
            dailyBalance = balance,
            isProcessingAi = isProcessing,
            userNotification = notification
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MobileDashboardUiState()
    )

    fun recordMealFromInput(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isProcessing.value = true
            _userNotification.value = "Consultando a Gemini AI..."
            val result = repository.processAndRecordMeal(text)
            _isProcessing.value = false
            result.onSuccess { balance ->
                _userNotification.value = "¡Comida registrada exitosamente!"
                // Difundir inmediatamente al reloj
                wearSyncClient.broadcastBalanceToAllConnectedWatches(balance)
            }.onFailure { e ->
                _userNotification.value = "Error al procesar: ${e.message}"
            }
        }
    }

    fun updateTargetCalories(newTarget: Int) {
        if (newTarget <= 0) return
        viewModelScope.launch {
            repository.updateTargetCalories(newTarget)
            val updatedBalance = repository.calculateCurrentBalance()
            wearSyncClient.broadcastBalanceToAllConnectedWatches(updatedBalance)
        }
    }

    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
            val updatedBalance = repository.calculateCurrentBalance()
            wearSyncClient.broadcastBalanceToAllConnectedWatches(updatedBalance)
        }
    }

    fun clearNotification() {
        _userNotification.value = null
    }

    class Factory(
        private val repository: MealRepository,
        private val wearSyncClient: WearSyncClient
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MobileDashboardViewModel(repository, wearSyncClient) as T
        }
    }
}

