package com.example.salubris.ui.screens.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.Purple80
import com.example.salubris.ui.theme.submitColor

@Composable
fun ProfileSetupTab(
    isLoading: Boolean,
    hasProfile: Boolean,
    recommendedCalories: Int?,
    age: Int?,
    sex: String?,
    heightCm: Int?,
    weightKg: Double?,
    activityLevel: String?,
    goal: String?,
    onSetupClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(ContainerBackground, RoundedCornerShape(10.dp))
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Your nutritional profile",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.W500
        )

        if (hasProfile && recommendedCalories != null) {
            // Profile exists – show all stored data
            Text(
                text = "✅ Profile complete",
                color = submitColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Personal Info",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Age: ${age ?: "—"} years",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "• Sex: ${sex?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—"}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "• Height: ${heightCm ?: "—"} cm",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "• Weight: ${weightKg ?: "—"} kg",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Activity & Goal",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Activity: ${formatActivityLevel(activityLevel)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "• Goal: ${formatGoal(goal)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Recommended daily calories: $recommendedCalories kcal",
                color = Purple80,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "⚠️ Disclaimer: These are only recommendations. Consult a healthcare professional before making significant dietary changes.",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        } else {
            // No profile – show setup prompt
            Text(
                text = "No profile data found. Click below to set up your age, sex, height, weight, goals and activity level.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        Button(
            onClick = onSetupClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (hasProfile) "Update profile" else "Set up profile")
        }
    }
}

private fun formatActivityLevel(level: String?): String {
    return when (level) {
        "SEDENTARY" -> "Sedentary (little or no exercise)"
        "LIGHT" -> "Light exercise (1-3 days/week)"
        "MODERATE" -> "Moderate exercise (3-5 days/week)"
        "ACTIVE" -> "Active (6-7 days/week)"
        "VERY_ACTIVE" -> "Very active (hard daily exercise)"
        else -> level ?: "—"
    }
}

private fun formatGoal(goal: String?): String {
    return when (goal) {
        "EXTREME_LOSS" -> "Extreme weight loss (1000 kcal deficit)"
        "MODERATE_LOSS" -> "Moderate weight loss (500 kcal deficit)"
        "MAINTAIN" -> "Maintain weight"
        "MODERATE_GAIN" -> "Moderate weight gain (500 kcal surplus)"
        "EXTREME_GAIN" -> "Extreme weight gain (1000 kcal surplus)"
        else -> goal ?: "—"
    }
}