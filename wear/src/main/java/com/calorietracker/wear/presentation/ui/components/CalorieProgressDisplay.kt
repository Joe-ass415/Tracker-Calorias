package com.calorietracker.wear.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import com.calorietracker.shared.model.DailyBalancePayload
import com.calorietracker.wear.presentation.theme.WearError
import com.calorietracker.wear.presentation.theme.WearPrimary

@Composable
fun CalorieProgressDisplay(
    balance: DailyBalancePayload,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val progress = balance.progressFraction
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "wear_calorie_progress"
    )

    val progressIndicatorColor = if (balance.isLimitExceeded) {
        WearError
    } else {
        WearPrimary
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Indicador circular exterior que envuelve toda la carátula
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            indicatorColor = progressIndicatorColor,
            trackColor = MaterialTheme.colors.surface,
            strokeWidth = 6.dp
        )

        content()
    }
}

