package com.calorietracker.wear.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.calorietracker.shared.model.MealRecordRequest
import com.calorietracker.shared.util.JsonConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

private val Context.offlineQueueDataStore: DataStore<Preferences> by preferencesDataStore(name = "wear_offline_queue")

class OfflineMealQueue(private val context: Context) {

    companion object {
        private val KEY_OFFLINE_ITEMS = stringPreferencesKey("offline_pending_meals")
    }

    val pendingCountFlow: Flow<Int> = context.offlineQueueDataStore.data.map { prefs ->
        val jsonString = prefs[KEY_OFFLINE_ITEMS] ?: "[]"
        try {
            val list = JsonConfig.json.decodeFromString<List<MealRecordRequest>>(jsonString)
            list.size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun enqueue(request: MealRecordRequest) {
        context.offlineQueueDataStore.edit { prefs ->
            val existingJson = prefs[KEY_OFFLINE_ITEMS] ?: "[]"
            val currentList = try {
                JsonConfig.json.decodeFromString<List<MealRecordRequest>>(existingJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            currentList.add(request)
            prefs[KEY_OFFLINE_ITEMS] = JsonConfig.json.encodeToString(currentList)
        }
    }

    suspend fun getAll(): List<MealRecordRequest> {
        val prefs = context.offlineQueueDataStore.data.first()
        val existingJson = prefs[KEY_OFFLINE_ITEMS] ?: "[]"
        return try {
            JsonConfig.json.decodeFromString<List<MealRecordRequest>>(existingJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun remove(requestId: String) {
        context.offlineQueueDataStore.edit { prefs ->
            val existingJson = prefs[KEY_OFFLINE_ITEMS] ?: "[]"
            val currentList = try {
                JsonConfig.json.decodeFromString<List<MealRecordRequest>>(existingJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            currentList.removeAll { it.requestId == requestId }
            prefs[KEY_OFFLINE_ITEMS] = JsonConfig.json.encodeToString(currentList)
        }
    }

    suspend fun clear() {
        context.offlineQueueDataStore.edit { prefs ->
            prefs.remove(KEY_OFFLINE_ITEMS)
        }
    }
}
