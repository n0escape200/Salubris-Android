package com.example.salubris.nutrition

enum class NutritionIntent {
    NONE,
    SINGLE_FOOD,
    MULTI_FOOD,
    MEAL,
    GENERAL_NUTRITION
}

object NutritionIntentDetector {

    private val nutritionWords = listOf(
        "calories", "calorie", "kcal", "protein", "proteins",
        "fat", "fats", "carbs", "carbohydrates", "macros",
        "nutrition", "nutritional"
    )

    private val mealQuantityRegex = Regex("""(\d+)\s?(g|grams|gram|kg|ml)\s+([a-zA-Z ]+)""")

    private val questionWords =
        listOf("how", "what", "why", "when", "can", "should", "is", "are", "does", "do")

    fun detect(prompt: String): NutritionIntent {
        val lower = prompt.lowercase()

        if (!nutritionWords.any { lower.contains(it) }) {
            return NutritionIntent.NONE
        }

        if (mealQuantityRegex.containsMatchIn(lower)) {
            return NutritionIntent.MEAL
        }

        val multiSeparators = listOf(",", " and ", "&", "+")
        if (multiSeparators.any { lower.contains(it) }) {
            return NutritionIntent.MULTI_FOOD
        }

        val isQuestion = questionWords.any { lower.startsWith(it) } || lower.contains("?")

        val stopWords = setOf(
            "give", "me", "the", "for", "per", "of", "in", "a", "an", "is", "what", "how", "much",
            "many", "macros", "macro", "nutrition", "nutritional", "calories", "calorie",
            "kcal", "protein", "proteins", "fat", "fats", "carbs", "carbohydrates",
            "100g", "100", "g", "grams", "gram", "kg", "ml", "per", "100"
        )
        val cleaned = prompt.lowercase()
            .replace(Regex("\\b(\\d+)\\s?(g|grams|gram|kg|ml)\\b"), "")
            .split(" ")
            .filter { it !in stopWords && it.isNotBlank() }
            .joinToString(" ")
            .trim()

        if (isQuestion || cleaned.isEmpty() || cleaned.length <= 3) {
            return NutritionIntent.GENERAL_NUTRITION
        }

        return NutritionIntent.SINGLE_FOOD
    }
}