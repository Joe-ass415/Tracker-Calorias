package com.calorietracker.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.calorietracker.mobile.presentation.theme.CalorieTrackerMobileTheme
import com.calorietracker.mobile.presentation.ui.DashboardScreen
import com.calorietracker.mobile.presentation.viewmodel.MobileDashboardViewModel
import com.calorietracker.mobile.service.WearSyncClient

class MainActivity : ComponentActivity() {

    private val viewModel: MobileDashboardViewModel by viewModels {
        val app = application as MobileApp
        MobileDashboardViewModel.Factory(
            repository = app.mealRepository,
            wearSyncClient = WearSyncClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalorieTrackerMobileTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}

