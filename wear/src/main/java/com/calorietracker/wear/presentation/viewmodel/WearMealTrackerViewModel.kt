package com.calorietracker.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calorietracker.wear.data.repository.SendMealResult
import com.calorietracker.wear.data.repository.WearMealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WearMealTrackerViewModel(
    private val repository: WearMealRepository
) : ViewModel() {

    private val _isSending = MutableStateFlow(false)
    private val _isPhoneConnected = MutableStateFlow(true)
    private val _statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MealTrackerUiState> = combine(
        repository.balanceFlow,
        repository.pendingCountFlow,
        _isSending,
        _isPhoneConnected,
        _statusMessage
    ) { balance, pendingCount, isSending, isConnected, statusMsg ->
        MealTrackerUiState(
            balance = balance,
            pendingOfflineCount = pendingCount,
            isPhoneConnected = isConnected,
            isSending = isSending,
            statusMessage = statusMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MealTrackerUiState()
    )

    init {
        checkConnectivityAndSync()
    }

    fun checkConnectivityAndSync() {
        viewModelScope.launch {
            val connected = repository.isPhoneConnected()
            _isPhoneConnected.value = connected
            if (connected) {
                val flushed = repository.retryOfflineSync()
                if (flushed > 0) {
                    _statusMessage.value = "Sincronizados $flushed registros pendientes"
                }
            }
        }
    }

    fun onVoiceRecorded(voiceText: String) {
        if (voiceText.isBlank()) return
        viewModelScope.launch {
            _isSending.value = true
            _statusMessage.value = "Enviando: \"$voiceText\"..."

            when (val result = repository.recordMeal(voiceText)) {
                is SendMealResult.Success -> {
                    _statusMessage.value = result.message
                }
                is SendMealResult.QueuedOffline -> {
                    _isPhoneConnected.value = false
                    _statusMessage.value = result.reason
                }
                is SendMealResult.Error -> {
                    _statusMessage.value = result.error
                }
            }
            _isSending.value = false
        }
    }

    fun retryOfflineQueue() {
        viewModelScope.launch {
            _statusMessage.value = "Reintentando sincronización..."
            val flushed = repository.retryOfflineSync()
            _statusMessage.value = if (flushed > 0) {
                "¡$flushed registros enviados!"
            } else {
                "Sin conexión disponible."
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    class Factory(private val repository: WearMealRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WearMealTrackerViewModel(repository) as T
        }
    }
}

