package com.calorietracker.mobile

import android.app.Application
import com.calorietracker.mobile.data.ai.GeminiNutritionService
import com.calorietracker.mobile.data.local.AppDatabase
import com.calorietracker.mobile.data.local.UserPreferencesDataStore
import com.calorietracker.mobile.data.repository.MealRepository
import com.calorietracker.mobile.data.repository.MealRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MobileApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val database by lazy { AppDatabase.getInstance(this) }
    val userPreferences by lazy { UserPreferencesDataStore(this) }
    val aiParser by lazy { GeminiNutritionService() }

    val mealRepository: MealRepository by lazy {
        MealRepositoryImpl(
            mealDao = database.mealDao(),
            preferencesDataStore = userPreferences,
            aiNutritionParser = aiParser
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}

