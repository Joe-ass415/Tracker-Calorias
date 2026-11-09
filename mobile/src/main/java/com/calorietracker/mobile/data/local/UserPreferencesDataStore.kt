package com.calorietracker.mobile.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_TARGET_CALORIES = intPreferencesKey("target_calories")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        const val DEFAULT_TARGET_CALORIES = 2000
    }

    val targetCaloriesFlow: Flow<Int> = context.userDataStore.data.map { preferences ->
        preferences[KEY_TARGET_CALORIES] ?: DEFAULT_TARGET_CALORIES
    }

    val customApiKeyFlow: Flow<String?> = context.userDataStore.data.map { preferences ->
        preferences[KEY_GEMINI_API_KEY]
    }

    suspend fun setTargetCalories(calories: Int) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_TARGET_CALORIES] = calories
        }
    }

    suspend fun setCustomApiKey(apiKey: String) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_GEMINI_API_KEY] = apiKey
        }
    }
}

