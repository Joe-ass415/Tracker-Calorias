package com.calorietracker.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Esquema estricto devuelto por el modelo de IA (Google Gemini API).
 */
@Serializable
data class NutritionAiResponse(
    @SerialName("food_name")
    val foodName: String,

    @SerialName("calories")
    val calories: Int,

    @SerialName("protein_g")
    val proteinG: Double = 0.0,

    @SerialName("carbs_g")
    val carbsG: Double = 0.0,

    @SerialName("fat_g")
    val fatG: Double = 0.0,

    @SerialName("meal_type")
    val mealType: String = "SNACK",

    @SerialName("confidence")
    val confidence: String = "MEDIUM"
) {
    fun toDomainMealType(): MealType = MealType.fromString(mealType)
    fun toDomainConfidence(): ConfidenceLevel = ConfidenceLevel.fromString(confidence)
}

