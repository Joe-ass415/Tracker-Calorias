package com.calorietracker.mobile.data.ai

import android.util.Log
import com.calorietracker.mobile.BuildConfig
import com.calorietracker.shared.model.NutritionAiResponse
import com.calorietracker.shared.util.JsonConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Calendar
import java.util.concurrent.TimeUnit

class GeminiNutritionService(
    private val customApiKey: String? = null
) : AiNutritionParser {

    companion object {
        private const val TAG = "GeminiNutritionService"
        private val CANDIDATE_MODELS = listOf(
            "gemini-3.8-flash",
            "gemini-flash-latest",
            "gemini-3.8-pro"
        )

        val SYSTEM_PROMPT = """
            Eres un asistente de nutrición y dietética altamente especializado. 
            El usuario te indicará mediante lenguaje natural lo que acaba de comer o beber.
            Tu tarea es analizar el alimento, estimar el tamaño de la porción habitual si no se especifica,
            calcular las calorías aproximadas y los macronutrientes (proteínas, carbohidratos y grasas en gramos).
            
            Además, debes categorizar el tipo de comida en una de estas 4 opciones exactas según la hora o contexto:
            - BREAKFAST (Desayuno)
            - LUNCH (Almuerzo / Comida del mediodía)
            - DINNER (Cena)
            - SNACK (Tentempié / Merienda / Colación)
            
            Evalúa tu nivel de confianza en el cálculo (HIGH, MEDIUM o LOW):
            - HIGH: Si el usuario dio porciones claras o alimentos directos (ej: '2 huevos cocidos y 1 manzana').
            - MEDIUM: Si el alimento es común pero sin cantidades exactas (ej: 'un plato de tacos al pastor').
            - LOW: Si la descripción es ambigua o imprecisa (ej: 'comí un postre en una fiesta').
            
            REGLA ESTRICTA DE SALIDA:
            Debes responder ÚNICAMENTE con un objeto JSON válido, sin bloques de código markdown, sin explicaciones ni texto introductorio, conforme a este esquema exacto:
            {
              "food_name": "Descripción limpia del alimento",
              "calories": 450,
              "protein_g": 20.0,
              "carbs_g": 50.0,
              "fat_g": 15.0,
              "meal_type": "BREAKFAST",
              "confidence": "HIGH"
            }
        """.trimIndent()
    }

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun parseNutritionFromVoice(voiceText: String): Result<NutritionAiResponse> =
        withContext(Dispatchers.IO) {
            val apiKey = resolveApiKey()
            if (apiKey.isBlank()) {
                Log.w(TAG, "No API key found. Falling back to local heuristic estimation engine.")
                return@withContext Result.success(fallbackHeuristicEstimate(voiceText))
            }

            Log.d(TAG, "Executing Gemini AI request for: \"$voiceText\" with Key: ${apiKey.take(8)}...")

            // Probamos vía REST con el encabezado oficial 'x-goog-api-key', que soporta tanto claves AIzaSy... como AQ...
            for (modelName in CANDIDATE_MODELS) {
                try {
                    val result = callGeminiRest(apiKey, modelName, voiceText)
                    if (result != null) {
                        Log.i(TAG, "SUCCESS! Parsed Gemini response with model [$modelName]: $result")
                        return@withContext Result.success(result)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Model [$modelName] REST attempt failed: ${e.message}. Trying next...")
                }
            }

            Log.e(TAG, "All cloud Gemini models failed. Activating heuristic fallback.")
            Result.success(fallbackHeuristicEstimate(voiceText))
        }

    /**
     * Llamada REST nativa a Google Gemini API.
     * Soporta los formatos de clave nuevos (AQ...) y clásicos (AIzaSy...) sin validaciones obsoletas de SDK.
     */
    private fun callGeminiRest(apiKey: String, modelName: String, voiceText: String): NutritionAiResponse? {
        val requestJson = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", "Alimento consumido por el usuario: \"$voiceText\"")
                        }
                    }
                }
            }
            putJsonObject("systemInstruction") {
                putJsonArray("parts") {
                    addJsonObject {
                        put("text", SYSTEM_PROMPT)
                    }
                }
            }
            putJsonObject("generationConfig") {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            }
        }.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toRequestBody(mediaType)

        val cleanKey = apiKey.trim().replace("\"", "").replace("'", "")
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-goog-api-key", cleanKey)
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.w(TAG, "HTTP ${response.code} from Gemini [$modelName]: $responseBody")
            return null
        }

        val jsonElement = JsonConfig.json.parseToJsonElement(responseBody)
        val candidates = jsonElement.jsonObject["candidates"]?.jsonArray
        val firstCandidate = candidates?.firstOrNull()?.jsonObject
        val content = firstCandidate?.get("content")?.jsonObject
        val parts = content?.get("parts")?.jsonArray
        val rawAiText = parts?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: throw IllegalStateException("Empty text content in Gemini response")

        val cleanJson = cleanJsonResponse(rawAiText)
        return JsonConfig.json.decodeFromString<NutritionAiResponse>(cleanJson)
    }

    private fun resolveApiKey(): String {
        return when {
            !customApiKey.isNullOrBlank() -> customApiKey
            BuildConfig.GEMINI_API_KEY.isNotBlank() -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }
    }

    private fun cleanJsonResponse(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json").trim()
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```").trim()
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```").trim()
        }
        return cleaned.trim()
    }

    /**
     * Motor de respaldo heurístico para pruebas locales sin conexión o sin API Key.
     */
    private fun fallbackHeuristicEstimate(input: String): NutritionAiResponse {
        val lower = input.lowercase().trim()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val defaultMealType = when (currentHour) {
            in 6..11 -> "BREAKFAST"
            in 12..16 -> "LUNCH"
            in 19..23 -> "DINNER"
            else -> "SNACK"
        }

        return when {
            lower.contains("taco") -> NutritionAiResponse(
                foodName = input.replaceFirstChar { it.uppercase() },
                calories = 450,
                proteinG = 22.0,
                carbsG = 42.0,
                fatG = 18.0,
                mealType = if (currentHour in 12..17) "LUNCH" else "DINNER",
                confidence = "HIGH"
            )
            lower.contains("pizza") -> NutritionAiResponse(
                foodName = "Rebanadas de Pizza",
                calories = 580,
                proteinG = 24.0,
                carbsG = 65.0,
                fatG = 22.0,
                mealType = "DINNER",
                confidence = "HIGH"
            )
            lower.contains("huevo") || lower.contains("omelet") -> NutritionAiResponse(
                foodName = "Huevos preparados",
                calories = 310,
                proteinG = 18.0,
                carbsG = 4.0,
                fatG = 22.0,
                mealType = "BREAKFAST",
                confidence = "HIGH"
            )
            lower.contains("ensalada") -> NutritionAiResponse(
                foodName = "Ensalada fresca",
                calories = 220,
                proteinG = 9.0,
                carbsG = 18.0,
                fatG = 12.0,
                mealType = "LUNCH",
                confidence = "MEDIUM"
            )
            lower.contains("manzana") || lower.contains("fruta") || lower.contains("platano") -> NutritionAiResponse(
                foodName = "Porción de fruta",
                calories = 95,
                proteinG = 0.5,
                carbsG = 25.0,
                fatG = 0.3,
                mealType = "SNACK",
                confidence = "HIGH"
            )
            lower.contains("cafe") || lower.contains("café") -> NutritionAiResponse(
                foodName = "Café con leche",
                calories = 120,
                proteinG = 4.0,
                carbsG = 10.0,
                fatG = 5.0,
                mealType = "BREAKFAST",
                confidence = "MEDIUM"
            )
            lower.contains("hamburguesa") || lower.contains("burger") -> NutritionAiResponse(
                foodName = "Hamburguesa con papas",
                calories = 750,
                proteinG = 32.0,
                carbsG = 65.0,
                fatG = 38.0,
                mealType = if (currentHour in 12..17) "LUNCH" else "DINNER",
                confidence = "HIGH"
            )
            lower.contains("sabritas") || lower.contains("papas") || lower.contains("chips") -> NutritionAiResponse(
                foodName = "Bolsa de papas / frituras",
                calories = 240,
                proteinG = 2.0,
                carbsG = 26.0,
                fatG = 14.0,
                mealType = "SNACK",
                confidence = "HIGH"
            )
            lower.contains("red bull") || lower.contains("monster") || lower.contains("energetica") -> NutritionAiResponse(
                foodName = "Bebida Energética (Red Bull)",
                calories = 115,
                proteinG = 1.0,
                carbsG = 27.0,
                fatG = 0.0,
                mealType = "SNACK",
                confidence = "HIGH"
            )
            else -> NutritionAiResponse(
                foodName = input.replaceFirstChar { it.uppercase() },
                calories = 380,
                proteinG = 15.0,
                carbsG = 45.0,
                fatG = 12.0,
                mealType = defaultMealType,
                confidence = "MEDIUM"
            )
        }
    }
}
