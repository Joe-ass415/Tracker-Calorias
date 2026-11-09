package com.calorietracker.wear

import android.app.Application
import com.calorietracker.wear.data.local.OfflineMealQueue
import com.calorietracker.wear.data.local.WearDataStore
import com.calorietracker.wear.data.repository.WearMealRepository

class WearApp : Application() {

    val wearDataStore by lazy { WearDataStore(this) }
    val offlineQueue by lazy { OfflineMealQueue(this) }

    val repository: WearMealRepository by lazy {
        WearMealRepository(
            context = this,
            wearDataStore = wearDataStore,
            offlineQueue = offlineQueue
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}

