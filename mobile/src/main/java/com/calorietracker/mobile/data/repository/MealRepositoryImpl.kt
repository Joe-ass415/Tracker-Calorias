package com.calorietracker.mobile.data.repository

import com.calorietracker.mobile.data.ai.AiNutritionParser
import com.calorietracker.mobile.data.local.dao.MealDao
import com.calorietracker.mobile.data.local.entity.MealEntity
import com.calorietracker.mobile.data.local.UserPreferencesDataStore
import com.calorietracker.shared.model.DailyBalancePayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MealRepositoryImpl(
    private val mealDao: MealDao,
    private val preferencesDataStore: UserPreferencesDataStore,
    private val aiNutritionParser: AiNutritionParser
) : MealRepository {

    override fun getTodayMeals(): Flow<List<MealEntity>> {
        val (start, end) = getDayBounds()
        return mealDao.getMealsForDay(start, end)
    }

    override fun getTargetCalories(): Flow<Int> {
        return preferencesDataStore.targetCaloriesFlow
    }

    override fun getDailyBalanceFlow(): Flow<DailyBalancePayload> {
        return combine(getTodayMeals(), getTargetCalories()) { meals, target ->
            buildBalancePayload(meals, target)
        }
    }

    override suspend fun processAndRecordMeal(
        voiceText: String,
        requestId: String
    ): Result<DailyBalancePayload> {
        return runCatching {
            // 1. Invocar IA para extraer desglose nutricional
            val aiResult = aiNutritionParser.parseNutritionFromVoice(voiceText)
                .getOrThrow()

            // 2. Persistir en Room
            val entity = MealEntity(
                foodName = aiResult.foodName,
                calories = aiResult.calories,
                proteinG = aiResult.proteinG,
                carbsG = aiResult.carbsG,
                fatG = aiResult.fatG,
                mealType = aiResult.mealType,
                confidence = aiResult.confidence,
                timestamp = System.currentTimeMillis(),
                rawVoicePrompt = voiceText,
                requestId = requestId
            )
            mealDao.insertMeal(entity)

            // 3. Recalcular balance diario actualizado
            calculateCurrentBalance()
        }
    }

    override suspend fun calculateCurrentBalance(): DailyBalancePayload {
        val (start, end) = getDayBounds()
        val meals = mealDao.getMealsForDayDirect(start, end)
        val target = preferencesDataStore.targetCaloriesFlow.first()
        return buildBalancePayload(meals, target)
    }

    override suspend fun updateTargetCalories(calories: Int) {
        preferencesDataStore.setTargetCalories(calories)
    }

    override suspend fun deleteMeal(mealId: Long) {
        mealDao.deleteMealById(mealId)
    }

    override suspend fun clearHistory() {
        mealDao.clearAll()
    }

    private fun buildBalancePayload(meals: List<MealEntity>, target: Int): DailyBalancePayload {
        val consumed = meals.sumOf { it.calories }
        val remaining = (target - consumed).coerceAtLeast(0)
        val isExceeded = consumed > target
        val latestMeal = meals.maxByOrNull { it.timestamp }

        return DailyBalancePayload(
            consumedCalories = consumed,
            targetCalories = target,
            remainingCalories = remaining,
            isLimitExceeded = isExceeded,
            lastMealName = latestMeal?.foodName ?: "",
            lastMealCalories = latestMeal?.calories ?: 0
        )
    }

    private fun getDayBounds(): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = calendar.timeInMillis - 1
        return Pair(start, end)
    }
}

