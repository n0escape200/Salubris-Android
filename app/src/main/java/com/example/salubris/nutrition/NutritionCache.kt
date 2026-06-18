package com.example.salubris.nutrition

object NutritionCache {

    private val cache =
        mutableMapOf<String, NutritionData>()

    fun get(food: String): NutritionData? {
        return cache[food.lowercase()]
    }

    fun put(food: String, data: NutritionData) {
        cache[food.lowercase()] = data
    }
}