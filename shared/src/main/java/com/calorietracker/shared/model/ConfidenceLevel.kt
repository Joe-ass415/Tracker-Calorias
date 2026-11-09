package com.calorietracker.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW;

    companion object {
        fun fromString(value: String?): ConfidenceLevel {
            return when (value?.trim()?.uppercase()) {
                "HIGH", "ALTA" -> HIGH
                "MEDIUM", "MEDIA" -> MEDIUM
                "LOW", "BAJA" -> LOW
                else -> MEDIUM
            }
        }
    }
}

