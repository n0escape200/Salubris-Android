package com.example.salubris.nutrition

interface WebSearchService {
    suspend fun searchFood(query: String): List<FoodNutritionData>
}

// Temporary stub implementation – replace with real API calls
class StubWebSearchService : WebSearchService {
    override suspend fun searchFood(query: String): List<FoodNutritionData> {
        // Return an empty list for now; your actual implementation will hit Google/Bing/etc.
        return emptyList()
    }
}

class DemoWebSearchService : WebSearchService {
    override suspend fun searchFood(query: String): List<FoodNutritionData> {
        // Hardcoded fallback for "chicken breast" to test the UI flow
        if (query.contains("chicken", ignoreCase = true)) {
            return listOf(
                FoodNutritionData(
                    name = "Chicken breast, raw",
                    calories = 120.0,
                    protein = 23.0,
                    carbs = 0.0,
                    fat = 2.6,
                    sourceUrl = "https://fdc.nal.usda.gov/fdc-app.html#/food-details/171477/nutrients",
                    sourceName = "USDA FoodData Central"
                )
            )
        }
        return emptyList()
    }
}