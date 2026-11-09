package com.calorietracker.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class MealType(val displayName: String) {
    BREAKFAST("Desayuno"),
    LUNCH("Almuerzo"),
    DINNER("Cena"),
    SNACK("Snack");

    companion object {
        fun fromString(value: String?): MealType {
            return when (value?.trim()?.uppercase()) {
                "BREAKFAST", "DESAYUNO" -> BREAKFAST
                "LUNCH", "ALMUERZO", "COMIDA" -> LUNCH
                "DINNER", "CENA" -> DINNER
                "SNACK", "MERIENDA", "COLACIÓN" -> SNACK
                else -> SNACK
            }
        }
    }
}

