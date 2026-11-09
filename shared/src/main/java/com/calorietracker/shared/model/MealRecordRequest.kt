package com.calorietracker.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payload emitido desde Wear OS con la orden de voz transcrita o metadatos de captura.
 */
@Serializable
data class MealRecordRequest(
    @SerialName("request_id")
    val requestId: String,

    @SerialName("voice_text")
    val voiceText: String,

    @SerialName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @SerialName("is_offline_retry")
    val isOfflineRetry: Boolean = false
)

