package com.example.salubris.nutrition

data class FoodNutritionData(
    val name: String,
    val calories: Double,  // per 100g (or scaled if already adjusted)
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sourceUrl: String,
    val sourceName: String
)