package com.calorietracker.mobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calorietracker.shared.model.MealType

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "food_name")
    val foodName: String,

    @ColumnInfo(name = "calories")
    val calories: Int,

    @ColumnInfo(name = "protein_g")
    val proteinG: Double,

    @ColumnInfo(name = "carbs_g")
    val carbsG: Double,

    @ColumnInfo(name = "fat_g")
    val fatG: Double,

    @ColumnInfo(name = "meal_type")
    val mealType: String,

    @ColumnInfo(name = "confidence")
    val confidence: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "raw_voice_prompt")
    val rawVoicePrompt: String = "",

    @ColumnInfo(name = "request_id")
    val requestId: String = ""
) {
    fun toDomainMealType(): MealType = MealType.fromString(mealType)
}

