package com.calorietracker.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val WearColorPalette = Colors(
    primary = WearPrimary,
    primaryVariant = WearPrimaryDark,
    secondary = WearSecondary,
    background = WearBackground,
    surface = WearSurface,
    error = WearError,
    onPrimary = WearBackground,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White
)

@Composable
fun CalorieTrackerWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColorPalette,
        content = content
    )
}

