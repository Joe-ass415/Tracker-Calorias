package com.calorietracker.wear.data.repository

import android.content.Context
import android.util.Log
import com.calorietracker.shared.constants.WearCommunicationRoutes
import com.calorietracker.shared.model.DailyBalancePayload
import com.calorietracker.shared.model.MealRecordRequest
import com.calorietracker.shared.util.toJsonByteArray
import com.calorietracker.wear.data.local.OfflineMealQueue
import com.calorietracker.wear.data.local.WearDataStore
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class SendMealResult {
    data class Success(val message: String) : SendMealResult()
    data class QueuedOffline(val reason: String) : SendMealResult()
    data class Error(val error: String) : SendMealResult()
}

class WearMealRepository(
    private val context: Context,
    private val wearDataStore: WearDataStore,
    private val offlineQueue: OfflineMealQueue
) {
    companion object {
        private const val TAG = "WearMealRepository"
    }

    private val messageClient by lazy { Wearable.getMessageClient(context) }
    private val nodeClient by lazy { Wearable.getNodeClient(context) }

    val balanceFlow: Flow<DailyBalancePayload> = wearDataStore.balanceFlow
    val pendingCountFlow: Flow<Int> = offlineQueue.pendingCountFlow

    suspend fun isPhoneConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            val nodes = nodeClient.connectedNodes.await()
            nodes.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun recordMeal(voiceText: String): SendMealResult = withContext(Dispatchers.IO) {
        val requestId = UUID.randomUUID().toString()
        val request = MealRecordRequest(
            requestId = requestId,
            voiceText = voiceText,
            timestamp = System.currentTimeMillis()
        )

        try {
            val connectedNodes = nodeClient.connectedNodes.await()
            Log.d(TAG, "Checking connected nodes: Found ${connectedNodes.size} node(s): ${connectedNodes.map { "${it.displayName} (${it.id})" }}")
            if (connectedNodes.isEmpty()) {
                Log.w(TAG, "No phone connected. Queuing request locally in OfflineMealQueue.")
                offlineQueue.enqueue(request)
                return@withContext SendMealResult.QueuedOffline("Sin conexión al teléfono. Guardado en cola local.")
            }

            val payloadBytes = request.toJsonByteArray()
            var sentToAtLeastOne = false

            for (node in connectedNodes) {
                try {
                    messageClient.sendMessage(
                        node.id,
                        WearCommunicationRoutes.MEAL_RECORD_PATH,
                        payloadBytes
                    ).await()
                    sentToAtLeastOne = true
                    Log.d(TAG, "Sent meal to phone node ${node.displayName} (${node.id})")
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending to node ${node.id}: ${e.message}")
                }
            }

            if (sentToAtLeastOne) {
                // Si teníamos items pendientes offline, intentamos sincronizarlos en segundo plano
                syncOfflineQueueInternal()
                SendMealResult.Success("Enviado al teléfono para análisis con IA...")
            } else {
                offlineQueue.enqueue(request)
                SendMealResult.QueuedOffline("Fallo de red. Guardado en cola offline.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during meal record: ${e.message}", e)
            offlineQueue.enqueue(request)
            SendMealResult.QueuedOffline("Guardado offline por error de enlace.")
        }
    }

    /**
     * Reintenta enviar todos los eventos almacenados en la cola local SQLite/DataStore.
     */
    suspend fun retryOfflineSync(): Int = withContext(Dispatchers.IO) {
        syncOfflineQueueInternal()
    }

    private suspend fun syncOfflineQueueInternal(): Int {
        val pending = offlineQueue.getAll()
        if (pending.isEmpty()) return 0

        val connectedNodes = try {
            nodeClient.connectedNodes.await()
        } catch (e: Exception) {
            return 0
        }

        if (connectedNodes.isEmpty()) return 0

        var flushedCount = 0
        for (request in pending) {
            val retryRequest = request.copy(isOfflineRetry = true)
            val bytes = retryRequest.toJsonByteArray()
            var delivered = false

            for (node in connectedNodes) {
                try {
                    messageClient.sendMessage(
                        node.id,
                        WearCommunicationRoutes.MEAL_RECORD_PATH,
                        bytes
                    ).await()
                    delivered = true
                } catch (e: Exception) {
                    Log.w(TAG, "Retry send failed for ${request.requestId}: ${e.message}")
                }
            }

            if (delivered) {
                offlineQueue.remove(request.requestId)
                flushedCount++
            }
        }
        return flushedCount
    }

    suspend fun saveBalance(payload: DailyBalancePayload) {
        wearDataStore.saveBalance(payload)
    }
}

