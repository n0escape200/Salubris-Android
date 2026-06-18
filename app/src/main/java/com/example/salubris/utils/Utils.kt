package com.example.salubris.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salubris.database.entities.Product
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId

fun Float.truncate2Decimals(): Float {
    return (this * 100).toInt() / 100f
}

@RequiresApi(Build.VERSION_CODES.O)
fun getStartOfDay(timestamp: Long): Long {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun calculateMacrosForProduct(product: Product, amountInGrams: Float): Map<String, Float> {
    val factor = amountInGrams / 100f
    return mapOf(
        "calories" to product.calories * factor,
        "protein" to product.protein * factor,
        "carbs" to product.carbs * factor,
        "fats" to product.fats * factor
    )
}

@Composable
fun ProductNutritionLabel(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(45, 45, 45), RoundedCornerShape(10.dp))
            .border(1.dp, Color.White, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            Vocabulary.get().nutritionPer100g,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(Vocabulary.get().calories, color = Color.White)
            Text("${product.calories}", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(Vocabulary.get().protein, color = Color.White)
            Text("${product.protein} g", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(Vocabulary.get().carbs, color = Color.White)
            Text("${product.carbs} g", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(Vocabulary.get().fats, color = Color.White)
            Text("${product.fats} g", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

fun Any?.toFloatSafe(): Float {
    return when (this) {
        is Float -> this
        is Double -> this.toFloat()
        is Int -> this.toFloat()
        is String -> this
            .replace(",", ".") // EU format support
            .trim()
            .toFloatOrNull() ?: 0f

        else -> 0f
    }
}

enum class Sex { MALE, FEMALE }
enum class Goal { LOSE, MAINTAIN, GAIN }
enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2),
    LIGHT(1.375),
    MODERATE(1.55),
    ACTIVE(1.725),
    VERY_ACTIVE(1.9)
}

object CalorieCalculator {
    // Mifflin-St Jeor formula
    enum class GoalOption(val displayName: String, val calorieAdjustment: Int) {
        EXTREME_LOSS("Extreme weight loss (1000 kcal deficit)", -1000),
        MODERATE_LOSS("Moderate weight loss (500 kcal deficit)", -500),
        MAINTAIN("Maintain weight", 0),
        MODERATE_GAIN("Moderate weight gain (500 kcal surplus)", 500),
        EXTREME_GAIN("Extreme weight gain (1000 kcal surplus)", 1000)
    }

    fun calculateBMR(weightKg: Double, heightCm: Double, age: Int, sex: Sex): Double {
        return if (sex == Sex.MALE) {
            10 * weightKg + 6.25 * heightCm - 5 * age + 5
        } else {
            10 * weightKg + 6.25 * heightCm - 5 * age - 161
        }
    }

    fun calculateTDEE(bmr: Double, activity: ActivityLevel): Double {
        return bmr * activity.factor
    }

    fun calculateRecommendedCalories(tdee: Double, adjustment: Int): Int {
        return (tdee + adjustment).toInt()
    }
}

@Serializable
data class ExtractedFoodItem(
    val name: String,
    val grams: Double? = null  // null = ask for quantity
)