package com.example.salubris.ui.screens.subpages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salubris.database.viewmodels.SettingViewModel
import com.example.salubris.ui.components.GoalOptionUi
import com.example.salubris.ui.components.GoalSelector
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.components.StepProgressionModal
import com.example.salubris.ui.theme.Purple80
import com.example.salubris.ui.theme.cancelColor
import com.example.salubris.ui.theme.submitColor
import com.example.salubris.utils.ActivityLevel
import com.example.salubris.utils.CalorieCalculator
import com.example.salubris.utils.Sex
import com.example.salubris.utils.Vocabulary
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun UserDataSetupModal(
    viewModel: SettingViewModel,
    onDismiss: () -> Unit,
    onComplete: (Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(Sex.MALE) }
    var heightCm by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf(ActivityLevel.MODERATE) }
    var goalOption by remember { mutableStateOf(CalorieCalculator.GoalOption.MAINTAIN) }
    var calculatedTDEE by remember { mutableStateOf<Double?>(null) }

    val scope = rememberCoroutineScope()

    val step1: @Composable (onNext: () -> Unit, onBack: () -> Unit, isLast: Boolean) -> Unit =
        { onNext, _, _ ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    Vocabulary.get().basicInformation,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W500
                )
                Spacer(modifier = Modifier.height(16.dp))
                Input(
                    label = Vocabulary.get().yourName,
                    value = name,
                    onChange = { name = it },
                    keyboardType = KeyboardType.Text
                )
                Spacer(modifier = Modifier.height(12.dp))
                Input(
                    label = Vocabulary.get().ageYears,
                    value = age,
                    onChange = { age = it.filter { char -> char.isDigit() } },
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(Vocabulary.get().sex, color = Color.White)
                Row {
                    RadioButton(selected = sex == Sex.MALE, onClick = { sex = Sex.MALE })
                    Text(
                        Vocabulary.get().male,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = sex == Sex.FEMALE, onClick = { sex = Sex.FEMALE })
                    Text(
                        Vocabulary.get().female,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Input(
                    label = Vocabulary.get().heightCm,
                    value = heightCm,
                    onChange = { heightCm = it.filter { char -> char.isDigit() } },
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(12.dp))
                Input(
                    label = Vocabulary.get().weightKg,
                    value = weightKg,
                    onChange = { weightKg = it.filter { char -> char.isDigit() || char == '.' } },
                    keyboardType = KeyboardType.Decimal
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        8.dp
                    )
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(
                            Vocabulary.get().cancel,
                            color = cancelColor
                        )
                    }
                    Button(
                        onClick = {
                            val ageInt = age.toIntOrNull()
                            val height = heightCm.toDoubleOrNull()
                            val weight = weightKg.toDoubleOrNull()
                            if (ageInt != null && height != null && weight != null) {
                                val bmr =
                                    CalorieCalculator.calculateBMR(weight, height, ageInt, sex)
                                calculatedTDEE = CalorieCalculator.calculateTDEE(bmr, activity)
                            }
                            onNext()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && age.isNotBlank() && heightCm.isNotBlank() && weightKg.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = submitColor)
                    ) { Text(Vocabulary.get().next) }
                }
            }
        }

    val step2: @Composable (onNext: () -> Unit, onBack: () -> Unit, isLast: Boolean) -> Unit =
        { onNext, onBack, _ ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    Vocabulary.get().activityLevelTitle,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W500
                )
                Text(Vocabulary.get().howOftenExercise, color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                ActivityLevel.values().forEach { a ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = activity == a, onClick = {
                            activity = a
                            val ageInt = age.toIntOrNull()
                            val height = heightCm.toDoubleOrNull()
                            val weight = weightKg.toDoubleOrNull()
                            if (ageInt != null && height != null && weight != null) {
                                val bmr =
                                    CalorieCalculator.calculateBMR(weight, height, ageInt, sex)
                                calculatedTDEE = CalorieCalculator.calculateTDEE(bmr, a)
                            }
                        })
                        Text(
                            text = when (a) {
                                ActivityLevel.SEDENTARY -> Vocabulary.get().sedentary
                                ActivityLevel.LIGHT -> Vocabulary.get().lightExercise
                                ActivityLevel.MODERATE -> Vocabulary.get().moderateExercise
                                ActivityLevel.ACTIVE -> Vocabulary.get().active
                                ActivityLevel.VERY_ACTIVE -> Vocabulary.get().veryActive
                            },
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        8.dp
                    )
                ) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                        Text(
                            Vocabulary.get().back,
                            color = cancelColor
                        )
                    }
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(
                            Vocabulary.get().cancel,
                            color = cancelColor
                        )
                    }
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = submitColor)
                    ) { Text(Vocabulary.get().next) }
                }
            }
        }

    val step3: @Composable (onNext: () -> Unit, onBack: () -> Unit, isLast: Boolean) -> Unit =
        { onNext, onBack, isLast ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    Vocabulary.get().yourGoal,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W500
                )
                Text(
                    text = if (calculatedTDEE != null) {
                        String.format(Vocabulary.get().baseMaintenance, calculatedTDEE!!.toInt())
                    } else {
                        Vocabulary.get().completePreviousSteps
                    },
                    color = Purple80,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (calculatedTDEE != null) {
                    val tdee = calculatedTDEE!!
                    val goalOptions = CalorieCalculator.GoalOption.values().map { option ->
                        val adjustedCalories = (tdee + option.calorieAdjustment).toInt()
                        val weeklyChangeKg = (option.calorieAdjustment * 7.0 / 7700).let {
                            if (it > 0) "+${"%.1f".format(it)} kg/week"
                            else "${"%.1f".format(it)} kg/week"
                        }
                        GoalOptionUi(
                            title = option.displayName,
                            subtitle = String.format(
                                Vocabulary.get().caloriesPerDayWeeklyChange,
                                adjustedCalories,
                                weeklyChangeKg
                            ),
                            isSelected = goalOption == option,
                            onSelect = { goalOption = option }
                        )
                    }

                    GoalSelector(options = goalOptions)
                } else {
                    Text(
                        Vocabulary.get().goBackFillInfo,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        8.dp
                    )
                ) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                        Text(
                            Vocabulary.get().back,
                            color = cancelColor
                        )
                    }
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(
                            Vocabulary.get().cancel,
                            color = cancelColor
                        )
                    }
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                        enabled = calculatedTDEE != null,
                        colors = ButtonDefaults.buttonColors(containerColor = submitColor)
                    ) {
                        Text(if (isLast) Vocabulary.get().calculate else Vocabulary.get().next)
                    }
                }
            }
        }

    StepProgressionModal(
        steps = listOf(step1, step2, step3),
        onComplete = {
            if (name.isBlank()) return@StepProgressionModal
            val ageInt = age.toIntOrNull() ?: return@StepProgressionModal
            val height = heightCm.toDoubleOrNull() ?: return@StepProgressionModal
            val weight = weightKg.toDoubleOrNull() ?: return@StepProgressionModal
            val tdee = calculatedTDEE ?: return@StepProgressionModal

            val recommended =
                CalorieCalculator.calculateRecommendedCalories(tdee, goalOption.calorieAdjustment)

            scope.launch {
                viewModel.saveSettings(
                    listOf(
                        "user_name" to name,
                        "user_age" to ageInt.toString(),
                        "user_sex" to sex.name,
                        "user_height_cm" to height.toString(),
                        "user_weight_kg" to weight.toString(),
                        "user_activity_level" to activity.name,
                        "user_goal" to goalOption.name,
                        "recommended_calories" to recommended.toString()
                    )
                )
            }
            onComplete(recommended)
            onDismiss()
        },
        onDismiss = onDismiss
    )
}