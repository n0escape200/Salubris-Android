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
import com.example.salubris.utils.Vocabulary

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
            Vocabulary.get().yourNutritionalProfile,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.W500
        )

        if (hasProfile && recommendedCalories != null) {
            // Profile exists – show all stored data
            Text(
                text = "✅ ${Vocabulary.get().profileComplete}",
                color = submitColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = Vocabulary.get().personalInfo,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• ${Vocabulary.get().ageLabel}: ${age ?: "—"} ${Vocabulary.get().years}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "• ${Vocabulary.get().sexLabel}: ${
                        sex?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—"
                    }",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "• ${Vocabulary.get().heightLabel}: ${heightCm ?: "—"} ${Vocabulary.get().cm}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "• ${Vocabulary.get().weightLabel}: ${weightKg ?: "—"} ${Vocabulary.get().kg}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = Vocabulary.get().activityAndGoal,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• ${Vocabulary.get().activityLabel}: ${formatActivityLevel(activityLevel)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "• ${Vocabulary.get().goalLabel}: ${formatGoal(goal)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = String.format(
                    Vocabulary.get().recommendedDailyCalories,
                    recommendedCalories
                ),
                color = Purple80,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Vocabulary.get().disclaimerText,
                color = Color.LightGray,
                fontSize = 12.sp
            )
        } else {
            // No profile – show setup prompt
            Text(
                text = Vocabulary.get().noProfileDataFound,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        Button(
            onClick = onSetupClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (hasProfile) Vocabulary.get().updateProfile else Vocabulary.get().setupProfile)
        }
    }
}

private fun formatActivityLevel(level: String?): String {
    return when (level) {
        "SEDENTARY" -> Vocabulary.get().sedentary
        "LIGHT" -> Vocabulary.get().lightExercise
        "MODERATE" -> Vocabulary.get().moderateExercise
        "ACTIVE" -> Vocabulary.get().active
        "VERY_ACTIVE" -> Vocabulary.get().veryActive
        else -> level ?: "—"
    }
}

private fun formatGoal(goal: String?): String {
    return when (goal) {
        "EXTREME_LOSS" -> Vocabulary.get().extremeLoss
        "MODERATE_LOSS" -> Vocabulary.get().moderateLoss
        "MAINTAIN" -> Vocabulary.get().maintain
        "MODERATE_GAIN" -> Vocabulary.get().moderateGain
        "EXTREME_GAIN" -> Vocabulary.get().extremeGain
        else -> goal ?: "—"
    }
}