package com.calorietracker.wear.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.calorietracker.shared.model.DailyBalancePayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wearDataStore: DataStore<Preferences> by preferencesDataStore(name = "wear_balance_preferences")

class WearDataStore(private val context: Context) {

    companion object {
        val KEY_CONSUMED = intPreferencesKey("consumed_calories")
        val KEY_TARGET = intPreferencesKey("target_calories")
        val KEY_REMAINING = intPreferencesKey("remaining_calories")
        val KEY_EXCEEDED = booleanPreferencesKey("is_limit_exceeded")
        val KEY_LAST_MEAL_NAME = stringPreferencesKey("last_meal_name")
        val KEY_LAST_MEAL_CALORIES = intPreferencesKey("last_meal_calories")
        val KEY_UPDATED_AT = longPreferencesKey("updated_at")
    }

    val balanceFlow: Flow<DailyBalancePayload> = context.wearDataStore.data.map { prefs ->
        DailyBalancePayload(
            consumedCalories = prefs[KEY_CONSUMED] ?: 0,
            targetCalories = prefs[KEY_TARGET] ?: 2000,
            remainingCalories = prefs[KEY_REMAINING] ?: 2000,
            isLimitExceeded = prefs[KEY_EXCEEDED] ?: false,
            lastMealName = prefs[KEY_LAST_MEAL_NAME] ?: "",
            lastMealCalories = prefs[KEY_LAST_MEAL_CALORIES] ?: 0,
            updatedAt = prefs[KEY_UPDATED_AT] ?: 0L
        )
    }

    suspend fun saveBalance(payload: DailyBalancePayload) {
        context.wearDataStore.edit { prefs ->
            prefs[KEY_CONSUMED] = payload.consumedCalories
            prefs[KEY_TARGET] = payload.targetCalories
            prefs[KEY_REMAINING] = payload.remainingCalories
            prefs[KEY_EXCEEDED] = payload.isLimitExceeded
            prefs[KEY_LAST_MEAL_NAME] = payload.lastMealName
            prefs[KEY_LAST_MEAL_CALORIES] = payload.lastMealCalories
            prefs[KEY_UPDATED_AT] = payload.updatedAt
        }
    }
}

