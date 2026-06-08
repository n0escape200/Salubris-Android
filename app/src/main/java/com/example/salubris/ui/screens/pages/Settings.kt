package com.example.salubris.ui.screens.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.ui.components.PageModal
import com.example.salubris.ui.screens.subpages.UserDataSetupModal
import com.example.salubris.database.viewmodels.SettingViewModel
import com.example.salubris.database.viewmodels.settingsViewModelFactory

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Settings(
    viewModel: SettingViewModel = viewModel(
        factory = settingsViewModelFactory(LocalContext.current)
    )
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var showSetupModal by remember { mutableStateOf(false) }

    // Build a map of all settings for easy lookup
    val settingsMap = remember(settings) { settings.associate { it.name to it.value } }

    // Extract profile values
    val hasProfile = settingsMap.containsKey("recommended_calories")
    val recommendedCalories = settingsMap["recommended_calories"]?.toIntOrNull()
    val age = settingsMap["user_age"]?.toIntOrNull()
    val sex = settingsMap["user_sex"]
    val heightCm = settingsMap["user_height_cm"]?.toDoubleOrNull()?.toInt()
    val weightKg = settingsMap["user_weight_kg"]?.toDoubleOrNull()
    val activityLevel = settingsMap["user_activity_level"]
    val goal = settingsMap["user_goal"]


    val tabs: Map<String, @Composable () -> Unit> = mapOf(
        "Profile Setup" to {
            ProfileSetupTab(
                isLoading = isLoading,
                hasProfile = hasProfile,
                recommendedCalories = recommendedCalories,
                age = age,
                sex = sex,
                heightCm = heightCm,
                weightKg = weightKg,
                activityLevel = activityLevel,
                goal = goal,
                onSetupClick = { showSetupModal = true }
            )
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Settings",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.W600
        )

        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        error?.let {
            Text(it, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(8.dp))
        }

        PageModal(tabs)
    }

    if (showSetupModal) {
        UserDataSetupModal(
            viewModel = viewModel,
            onDismiss = { showSetupModal = false },
            onComplete = { _ ->
                showSetupModal = false
            }
        )
    }
}