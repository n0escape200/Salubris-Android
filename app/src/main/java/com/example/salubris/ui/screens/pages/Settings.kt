package com.example.salubris.ui.screens.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.viewmodels.SettingViewModel
import com.example.salubris.database.viewmodels.settingsViewModelFactory
import com.example.salubris.ui.components.PageModal
import com.example.salubris.ui.screens.subpages.UserDataSetupModal
import com.example.salubris.utils.Vocabulary
import kotlinx.coroutines.delay

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

    // Language
    val currentLanguage = settingsMap["language"] ?: "en"

    // Update Vocabulary when language changes from outside (e.g., when the app starts)
    LaunchedEffect(currentLanguage) {
        Vocabulary.setLanguage(currentLanguage)
    }

    // State for loading dialog
    var showLoadingDialog by remember { mutableStateOf(false) }

    // Automatically hide the dialog after a delay (give time for recomposition)
    LaunchedEffect(showLoadingDialog) {
        if (showLoadingDialog) {
            delay(800) // slightly longer to ensure UI is fully redrawn
            showLoadingDialog = false
        }
    }

    // Tabs: "Profile Setup" (original) + "Language"
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
        },
        "Language" to {
            LanguageTab(
                onLanguageSelected = { newLang ->
                    // Show loading dialog
                    showLoadingDialog = true

                    // Update Vocabulary immediately and save to database
                    Vocabulary.setLanguage(newLang)
                    viewModel.saveSetting("language", newLang)
                }
            )
        }
    )

    // Main content
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

    // Full‑screen loading dialog (covers everything, prevents interaction)
    if (showLoadingDialog) {
        Dialog(
            onDismissRequest = { /* Do nothing, prevent dismissal */ },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text(
                        "Applying language...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
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

// Language tab – observes Vocabulary.currentLanguage directly
@Composable
fun LanguageTab(
    onLanguageSelected: (String) -> Unit
) {
    // Observe the current language from Vocabulary's state flow
    val currentLanguage by Vocabulary.currentLanguage.collectAsState()

    val languages = listOf(
        "en" to Vocabulary.get().english,
        "es" to Vocabulary.get().spanish,
        "fr" to Vocabulary.get().french,
        "de" to Vocabulary.get().german,
        "ro" to Vocabulary.get().romanian
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            Vocabulary.get().selectLanguage,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        languages.forEach { (code, displayName) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentLanguage == code,
                    onClick = { onLanguageSelected(code) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF4DB8FF),
                        unselectedColor = Color.Gray
                    )
                )
                Text(
                    displayName,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}