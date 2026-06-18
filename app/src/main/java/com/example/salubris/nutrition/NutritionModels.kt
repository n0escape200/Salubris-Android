package com.example.salubris.nutrition

data class NutritionData(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val source: String,
    val sourceUrl: String? = null
)

data class MealItem(
    val name: String,
    val grams: Double
)

data class MealResult(
    val items: List<Pair<MealItem, NutritionData>>,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)