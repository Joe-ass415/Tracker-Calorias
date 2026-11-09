package com.calorietracker.mobile.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calorietracker.mobile.data.local.entity.MealEntity
import com.calorietracker.shared.model.MealType

@Composable
fun MealSectionList(
    mealsByType: Map<MealType, List<MealEntity>>,
    onDeleteMeal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val orderedSections = listOf(
        MealType.BREAKFAST,
        MealType.LUNCH,
        MealType.DINNER,
        MealType.SNACK
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val hasAnyMeal = mealsByType.values.any { it.isNotEmpty() }

        if (!hasAnyMeal) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aún no hay comidas registradas hoy",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Di lo que comiste en tu Smartwatch o usa el botón inferior.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            orderedSections.forEach { sectionType ->
                val meals = mealsByType[sectionType] ?: emptyList()
                if (meals.isNotEmpty()) {
                    MealTypeSection(
                        mealType = sectionType,
                        meals = meals,
                        onDeleteMeal = onDeleteMeal
                    )
                }
            }
        }
    }
}

@Composable
private fun MealTypeSection(
    mealType: MealType,
    meals: List<MealEntity>,
    onDeleteMeal: (Long) -> Unit
) {
    val totalCalories = meals.sumOf { it.calories }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mealType.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$totalCalories kcal",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        meals.forEach { meal ->
            MealItemCard(
                meal = meal,
                onDelete = onDeleteMeal
            )
        }
    }
}

