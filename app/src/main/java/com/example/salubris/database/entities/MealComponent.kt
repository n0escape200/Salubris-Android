package com.example.salubris.database.entities

data class MealComponent(
    val productName: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fats: Float,
    val quantity: Float, // grams of this ingredient in the recipe
    val resolvedProductId: Int? = null, // null if not resolved (draft)
    val isDraft: Boolean = false        // true if product not found in DB
)