package com.calorietracker.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payload ligero de balance diario enviado de vuelta al reloj inteligente
 * para actualizar la carátula o pantalla principal en Compose.
 */
@Serializable
data class DailyBalancePayload(
    @SerialName("consumed_calories")
    val consumedCalories: Int,

    @SerialName("target_calories")
    val targetCalories: Int,

    @SerialName("remaining_calories")
    val remainingCalories: Int,

    @SerialName("is_limit_exceeded")
    val isLimitExceeded: Boolean,

    @SerialName("last_meal_name")
    val lastMealName: String = "",

    @SerialName("last_meal_calories")
    val lastMealCalories: Int = 0,

    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (targetCalories > 0) {
            (consumedCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}

