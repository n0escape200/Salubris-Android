package com.example.salubris.nutrition

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class NutritionService(
    private val client: OkHttpClient,
    private val usdaApiKey: String
) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun lookupFood(food: String): NutritionData? {
        NutritionCache.get(food)?.let { return it }

        val url = "https://api.nal.usda.gov/fdc/v1/foods/search".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("query", food)
            .addQueryParameter("pageSize", "1")
            .addQueryParameter("api_key", usdaApiKey)
            .build()

        val request = Request.Builder().url(url).build()
        val responseBody = client.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() ?: return null
            else return null
        }

        val search = json.decodeFromString<UsdaSearchResponse>(responseBody)
        val result = search.foods.firstOrNull() ?: return null

        val calories = nutrient(result, 1008)
        val protein = nutrient(result, 1003)
        val carbs = nutrient(result, 1005)
        val fat = nutrient(result, 1004)

        val data = NutritionData(
            name = result.description,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            source = "USDA"
        )
        NutritionCache.put(food, data)
        return data
    }

    suspend fun calculateMeal(items: List<MealItem>): MealResult {
        val resolved = mutableListOf<Pair<MealItem, NutritionData>>()
        var calories = 0.0
        var protein = 0.0
        var carbs = 0.0
        var fat = 0.0

        for (item in items) {
            val nutrition = lookupFood(item.name) ?: continue
            val factor = item.grams / 100.0
            calories += nutrition.calories * factor
            protein += nutrition.protein * factor
            carbs += nutrition.carbs * factor
            fat += nutrition.fat * factor
            resolved += item to nutrition
        }

        return MealResult(
            items = resolved,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat
        )
    }

    private fun nutrient(food: UsdaFood, id: Int): Double {
        return food.foodNutrients
            .firstOrNull { it.nutrientNumber == id.toString() }
            ?.value
            ?: 0.0
    }
}

@Serializable
data class UsdaSearchResponse(
    val foods: List<UsdaFood>
)

@Serializable
data class UsdaFood(
    val description: String,
    val foodNutrients: List<UsdaNutrient>
)

@Serializable
data class UsdaNutrient(
    @SerialName("nutrientNumber")
    val nutrientNumber: String,
    val value: Double
)