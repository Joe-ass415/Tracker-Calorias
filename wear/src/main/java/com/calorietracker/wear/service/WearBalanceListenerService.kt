package com.calorietracker.wear.service

import android.util.Log
import com.calorietracker.shared.constants.WearCommunicationRoutes
import com.calorietracker.shared.model.DailyBalancePayload
import com.calorietracker.shared.util.decodeFromJsonByteArray
import com.calorietracker.wear.WearApp
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Servicio en segundo plano para Wear OS que escucha en /meal/sync_balance
 * para actualizar el estado local en DataStore y refrescar la UI al instante.
 */
class WearBalanceListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearBalanceListener"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Wearable message received at path: ${messageEvent.path}")

        if (messageEvent.path == WearCommunicationRoutes.MEAL_SYNC_BALANCE_PATH) {
            val rawData = messageEvent.data
            serviceScope.launch {
                try {
                    val payload: DailyBalancePayload = rawData.decodeFromJsonByteArray()
                    Log.i(TAG, "Balance sync received from phone via Message: Consumed=${payload.consumedCalories}, Remaining=${payload.remainingCalories}")

                    val app = application as WearApp
                    app.wearDataStore.saveBalance(payload)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding balance payload: ${e.message}", e)
                }
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                if (uri.path == WearCommunicationRoutes.MEAL_SYNC_BALANCE_PATH) {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    val map = dataMapItem.dataMap
                    val payload = DailyBalancePayload(
                        consumedCalories = map.getInt("consumed_calories", 0),
                        targetCalories = map.getInt("target_calories", 2000),
                        remainingCalories = map.getInt("remaining_calories", 2000),
                        isLimitExceeded = map.getBoolean("is_limit_exceeded", false),
                        lastMealName = map.getString("last_meal_name", ""),
                        lastMealCalories = map.getInt("last_meal_calories", 0),
                        updatedAt = map.getLong("updated_at", System.currentTimeMillis())
                    )

                    Log.i(TAG, "Balance sync received from phone via DataClient: $payload")
                    serviceScope.launch {
                        val app = application as WearApp
                        app.wearDataStore.saveBalance(payload)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

