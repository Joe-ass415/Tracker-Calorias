package com.calorietracker.mobile.service

import android.util.Log
import com.calorietracker.mobile.MobileApp
import com.calorietracker.shared.constants.WearCommunicationRoutes
import com.calorietracker.shared.model.MealRecordRequest
import com.calorietracker.shared.util.decodeFromJsonByteArray
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Servicio en segundo plano que escucha peticiones de registro de comidas
 * provenientes del Smartwatch Wear OS en la ruta /meal/record.
 */
class MobileMealListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "MobileMealListener"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val wearSyncClient by lazy { WearSyncClient(applicationContext) }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Message received at path: ${messageEvent.path} from node: ${messageEvent.sourceNodeId}")

        if (messageEvent.path == WearCommunicationRoutes.MEAL_RECORD_PATH) {
            val sourceNodeId = messageEvent.sourceNodeId
            val rawData = messageEvent.data
            val app = application as MobileApp

            app.applicationScope.launch {
                try {
                    val request: MealRecordRequest = rawData.decodeFromJsonByteArray()
                    Log.i(TAG, "Processing meal record from wear: \"${request.voiceText}\" [ID: ${request.requestId}]")

                    val repository = app.mealRepository

                    // 1. Procesar con Gemini AI y registrar en Room
                    val balanceResult = repository.processAndRecordMeal(
                        voiceText = request.voiceText,
                        requestId = request.requestId
                    )

                    balanceResult.onSuccess { balance ->
                        Log.i(TAG, "Meal registered successfully! Cal: ${balance.consumedCalories}/${balance.targetCalories}")
                        // 2. Enviar payload de balance de retorno al reloj
                        wearSyncClient.sendBalanceToNode(sourceNodeId, balance)
                        wearSyncClient.syncBalanceViaDataClient(balance)
                    }.onFailure { error ->
                        Log.e(TAG, "Failed to parse/record meal: ${error.message}", error)
                        // Enviamos balance actual aún si falló el procesamiento
                        val currentBalance = repository.calculateCurrentBalance()
                        wearSyncClient.sendBalanceToNode(sourceNodeId, currentBalance)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error handling wear message payload: ${e.message}", e)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

