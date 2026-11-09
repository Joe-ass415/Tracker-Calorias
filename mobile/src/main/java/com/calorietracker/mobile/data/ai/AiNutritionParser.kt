package com.calorietracker.mobile.data.ai

import com.calorietracker.shared.model.NutritionAiResponse

interface AiNutritionParser {
    /**
     * Procesa la entrada en lenguaje natural de la voz del usuario y extrae
     * los datos nutricionales estructurados mediante el modelo de IA.
     */
    suspend fun parseNutritionFromVoice(voiceText: String): Result<NutritionAiResponse>
}

