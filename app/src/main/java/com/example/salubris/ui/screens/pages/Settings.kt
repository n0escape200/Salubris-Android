package com.example.salubris.ui.screens.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.repositories.SettingRepository
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.components.Multiselect
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.utils.FieldType
import com.example.salubris.utils.FormData
import com.example.salubris.viewmodels.SettingViewModel
import com.example.salubris.viewmodels.SettingViewModelFactory
import com.example.salubris.viewmodels.settingsViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun Settings(
    viewModel: SettingViewModel = viewModel(
        factory = settingsViewModelFactory(LocalContext.current)
    )
) {
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
//    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()

//    var showSuccessMessage by remember { mutableStateOf(false) }

    // Show success message when operation succeeds
//    LaunchedEffect(operationStatus) {
//        if (operationStatus is SettingViewModel.OperationStatus.Success) {
//            showSuccessMessage = true
//            kotlinx.coroutines.delay(3000)
//            showSuccessMessage = false
//            viewModel.resetOperationStatus()
//        }
//    }

    // Initialize form data with saved values when settings load
    val userData = remember {
        mutableStateListOf(
            FormData("Name", FieldType.STRING, ""),
            FormData("Age", FieldType.NUMBER, 0),
            FormData("Height(cm)", FieldType.NUMBER, 0),
            FormData("Weight", FieldType.NUMBER, 0),
        )
    }

    val goalData = remember {
        mutableStateListOf(
            FormData("Calories", FieldType.NUMBER, 0),
            FormData("Water", FieldType.NUMBER, 0),
            FormData("Steps", FieldType.NUMBER, 0)

        )
    }

    // Load saved values into form when settings change
    LaunchedEffect(settings) {
        userData[0] = userData[0].copy(value = viewModel.getSettingValue("user_name"))
        userData[1] = userData[1].copy(value = viewModel.getSettingValueAsInt("user_age"))
        userData[2] = userData[2].copy(value = viewModel.getSettingValueAsInt("user_height"))
        userData[3] = userData[3].copy(value = viewModel.getSettingValueAsInt("user_weight"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp,0.dp,0.dp,10.dp)
            .verticalScroll(rememberScrollState())
        ,
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

        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
        }

//        if (showSuccessMessage) {
//            Text(
//                text = "Settings saved successfully!",
//                color = Color.Green,
//                fontSize = 14.sp,
//                modifier = Modifier.padding(8.dp)
//            )
//        }

        Column(
            modifier = Modifier
                .background(ContainerBackground, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "User data",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.W500
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Input(
                    label = userData[0].name,
                    value = userData[0].value as? String ?: "",
                    onChange = { newValue ->
                        userData[0] = userData[0].copy(value = newValue)
                    },
                    onPress = {
                        scope.launch {
                            viewModel.saveSetting("user_name", userData[0].value as String)
                        }
                    }
                )

                Input(
                    label = userData[1].name,
                    value = (userData[1].value as? Int ?: 0).toString(),
                    onChange = { newValue ->
                        val numberValue = newValue.toIntOrNull() ?: 0
                        userData[1] = userData[1].copy(value = numberValue)
                    },
                    onPress = {
                        scope.launch {
                            viewModel.saveSetting("user_age", (userData[1].value as Int).toString())
                        }
                    }
                )

                Input(
                    label = userData[2].name,
                    value = (userData[2].value as? Int ?: 0).toString(),
                    onChange = { newValue ->
                        val numberValue = newValue.toIntOrNull() ?: 0
                        userData[2] = userData[2].copy(value = numberValue)
                    },
                    onPress = {
                        scope.launch {
                            viewModel.saveSetting("user_height", (userData[2].value as Int).toString())
                        }
                    }
                )

                Input(
                    label = userData[3].name,
                    value = (userData[3].value as? Int ?: 0).toString(),
                    onChange = { newValue ->
                        val numberValue = newValue.toIntOrNull() ?: 0
                        userData[3] = userData[3].copy(value = numberValue)
                    },
                    onPress = {
                        scope.launch {
                            viewModel.saveSetting("user_weight", (userData[3].value as Int).toString())
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val settingsToSave = listOf(
                                "user_name" to (userData[0].value as String),
                                "user_age" to (userData[1].value as Int).toString(),
                                "user_height" to (userData[2].value as Int).toString(),
                                "user_weight" to (userData[3].value as Int).toString()
                            )
                            viewModel.saveSettings(settingsToSave)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Save All Settings")
                }
            }
        }

        Column(
            modifier = Modifier
                .background(ContainerBackground, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Main goal",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.W500
            )
            Multiselect(arrayOf("Weight loss", "Weight gain"))
        }

        Column(
            modifier = Modifier
                .background(ContainerBackground, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ){
            Text(
                "Other goals",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.W500
            )
            Input(
                label = goalData[0].name,
                value = (goalData[0].value as? Int ?: 0).toString(),
                onChange = { newValue ->
                    val numberValue = newValue.toIntOrNull() ?: 0
                    goalData[0] = goalData[0].copy(value = numberValue)
                },
                onPress = {
                    scope.launch {
                        viewModel.saveSetting("goal_calories", (goalData[0].value as Int).toString())
                    }
                }
            )
            Input(
                label = goalData[1].name,
                value = (goalData[1].value as? Int ?: 0).toString(),
                onChange = { newValue ->
                    val numberValue = newValue.toIntOrNull() ?: 0
                    goalData[1] = goalData[1].copy(value = numberValue)
                },
                onPress = {
                    scope.launch {
                        viewModel.saveSetting("goal_water", (goalData[1].value as Int).toString())
                    }
                }
            )
            Input(
                label = goalData[2].name,
                value = (goalData[2].value as? Int ?: 0).toString(),
                onChange = { newValue ->
                    val numberValue = newValue.toIntOrNull() ?: 0
                    goalData[2] = goalData[2].copy(value = numberValue)
                },
                onPress = {
                    scope.launch {
                        viewModel.saveSetting("goal_steps", (goalData[2].value as Int).toString())
                    }
                }
            )
        }
    }
}