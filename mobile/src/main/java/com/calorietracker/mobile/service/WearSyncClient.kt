package com.calorietracker.mobile.service

import android.content.Context
import android.util.Log
import com.calorietracker.shared.constants.WearCommunicationRoutes
import com.calorietracker.shared.model.DailyBalancePayload
import com.calorietracker.shared.util.toJsonByteArray
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearSyncClient(private val context: Context) {

    companion object {
        private const val TAG = "WearSyncClient"
    }

    private val messageClient by lazy { Wearable.getMessageClient(context) }
    private val dataClient by lazy { Wearable.getDataClient(context) }
    private val nodeClient by lazy { Wearable.getNodeClient(context) }

    /**
     * Envía el balance directamente al nodo del reloj que originó la petición
     * mediante MessageClient (baja latencia).
     */
    suspend fun sendBalanceToNode(nodeId: String, payload: DailyBalancePayload): Boolean {
        return try {
            val bytes = payload.toJsonByteArray()
            messageClient.sendMessage(nodeId, WearCommunicationRoutes.MEAL_SYNC_BALANCE_PATH, bytes).await()
            Log.d(TAG, "Sent sync balance to node $nodeId: $payload")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message to node $nodeId: ${e.message}", e)
            false
        }
    }

    /**
     * Difunde el balance a todos los relojes conectados actualmente.
     */
    suspend fun broadcastBalanceToAllConnectedWatches(payload: DailyBalancePayload): Int {
        var sentCount = 0
        try {
            val connectedNodes = nodeClient.connectedNodes.await()
            val bytes = payload.toJsonByteArray()
            for (node in connectedNodes) {
                try {
                    messageClient.sendMessage(node.id, WearCommunicationRoutes.MEAL_SYNC_BALANCE_PATH, bytes).await()
                    sentCount++
                } catch (e: Exception) {
                    Log.w(TAG, "Error sending to node ${node.displayName}: ${e.message}")
                }
            }
            // También actualizamos el DataClient para sincronización persistente
            syncBalanceViaDataClient(payload)
        } catch (e: Exception) {
            Log.e(TAG, "Error broadcasting balance: ${e.message}", e)
        }
        return sentCount
    }

    /**
     * Sincronización persistente en el Wearable Data Layer (DataClient).
     * Garantiza que si el reloj estaba desconectado temporalmente, reciba el estado
     * tan pronto como se reconecte por Bluetooth o Wi-Fi.
     */
    suspend fun syncBalanceViaDataClient(payload: DailyBalancePayload) {
        try {
            val putDataMapReq = PutDataMapRequest.create(WearCommunicationRoutes.MEAL_SYNC_BALANCE_PATH).apply {
                dataMap.putInt("consumed_calories", payload.consumedCalories)
                dataMap.putInt("target_calories", payload.targetCalories)
                dataMap.putInt("remaining_calories", payload.remainingCalories)
                dataMap.putBoolean("is_limit_exceeded", payload.isLimitExceeded)
                dataMap.putString("last_meal_name", payload.lastMealName)
                dataMap.putInt("last_meal_calories", payload.lastMealCalories)
                dataMap.putLong("updated_at", payload.updatedAt)
            }
            val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()
            dataClient.putDataItem(putDataReq).await()
            Log.d(TAG, "Balance synced to DataClient successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error putting data item to DataClient: ${e.message}", e)
        }
    }
}

