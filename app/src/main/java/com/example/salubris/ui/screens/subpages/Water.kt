package com.example.salubris.ui.screens.subpages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.entities.WaterEntity
import com.example.salubris.database.viewmodels.SettingViewModel
import com.example.salubris.database.viewmodels.WaterViewModel
import com.example.salubris.database.viewmodels.settingsViewModelFactory
import com.example.salubris.database.viewmodels.waterViewModelFactory
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.cancelColor
import com.example.salubris.ui.theme.waterColor
import com.example.salubris.utils.Vocabulary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Water() {
    val context = LocalContext.current

    // ViewModels
    val waterViewModel: WaterViewModel = viewModel(factory = waterViewModelFactory(context))
    val settingViewModel: SettingViewModel = viewModel(factory = settingsViewModelFactory(context))

    // Load cup sizes from settings
    val settings by settingViewModel.settings.collectAsState()
    val settingsMap = remember(settings) { settings.associate { it.name to it.value } }

    // Default cup sizes: 250, 500, 750
    val defaultCup1 = 250
    val defaultCup2 = 500
    val defaultCup3 = 750

    // Parse from settings or fallback to defaults
    val cup1 = settingsMap["water_cup1"]?.toIntOrNull() ?: defaultCup1
    val cup2 = settingsMap["water_cup2"]?.toIntOrNull() ?: defaultCup2
    val cup3 = settingsMap["water_cup3"]?.toIntOrNull() ?: defaultCup3

    // State for cup sizes (will be saved to settings)
    var cupSizes by remember { mutableStateOf(Triple(cup1, cup2, cup3)) }

    // Save cup sizes to settings when they change
    LaunchedEffect(cupSizes) {
        settingViewModel.saveSetting("water_cup1", cupSizes.first.toString())
        settingViewModel.saveSetting("water_cup2", cupSizes.second.toString())
        settingViewModel.saveSetting("water_cup3", cupSizes.third.toString())
    }

    // Date picker state – using UTC to avoid timezone issues
    val todayMillis = LocalDate.now()
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = todayMillis)
    var showDatePicker by remember { mutableStateOf(false) }

    // Selected date (default today)
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Update selected date when date picker changes – using UTC consistently
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            selectedDate = Instant.ofEpochMilli(millis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
        }
    }

    // Tell ViewModel which date to display
    LaunchedEffect(selectedDate) {
        waterViewModel.setDate(selectedDate.toString())
    }

    // Load history
    LaunchedEffect(Unit) {
        waterViewModel.loadHistory()
    }

    // Observe data from ViewModel
    val entries by waterViewModel.todayEntries.collectAsState()
    val todayTotal by waterViewModel.todayTotal.collectAsState()
    val isLoading by waterViewModel.isLoading.collectAsState()
    val history by waterViewModel.history.collectAsState()

    // Dialog states
    var showEditCupDialog by remember { mutableStateOf(false) }
    var showRecommendDialog by remember { mutableStateOf(false) }

    // Temporary state for dialogs
    var tempCup1 by remember { mutableStateOf("") }
    var tempCup2 by remember { mutableStateOf("") }
    var tempCup3 by remember { mutableStateOf("") }
    var tempIntake by remember { mutableStateOf("") }

    // Recommended intake (can be saved to settings later)
    var recommendedIntake by remember { mutableStateOf(2000) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Date: ${selectedDate.format(dateFormatter)}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = waterColor)
                ) {
                    Text("Select Date", color = Color.White)
                }
            }

            // Total intake card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ContainerBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(Vocabulary.get().totalWaterIntake, color = Color.White, fontSize = 18.sp)
                    Text(
                        text = String.format(Vocabulary.get().mlValue, todayTotal),
                        color = waterColor,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick add buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CupButton(amount = cupSizes.first) {
                    waterViewModel.addWaterEntry(cupSizes.first, selectedDate.toString())
                }
                CupButton(amount = cupSizes.second) {
                    waterViewModel.addWaterEntry(cupSizes.second, selectedDate.toString())
                }
                CupButton(amount = cupSizes.third) {
                    waterViewModel.addWaterEntry(cupSizes.third, selectedDate.toString())
                }
            }

            // Edit cup sizes button
            TextButton(
                onClick = {
                    tempCup1 = cupSizes.first.toString()
                    tempCup2 = cupSizes.second.toString()
                    tempCup3 = cupSizes.third.toString()
                    showEditCupDialog = true
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = waterColor)
                Spacer(Modifier.width(4.dp))
                Text(Vocabulary.get().editCupSizes, color = waterColor)
            }

            // Recommended intake with progress bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ContainerBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Vocabulary.get().recommendedIntake, color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(Vocabulary.get().mlValue, recommendedIntake),
                                color = waterColor,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                tempIntake = recommendedIntake.toString()
                                showRecommendDialog = true
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = waterColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = (todayTotal.toFloat() / recommendedIntake).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = waterColor,
                        trackColor = Color.DarkGray
                    )
                    Text(
                        text = String.format(
                            Vocabulary.get().percentOfDailyGoal,
                            ((todayTotal.toFloat() / recommendedIntake * 100).toInt())
                        ),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Today's entries list
            Text(
                text = "Entries",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = waterColor)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(entries) { entry ->
                        HistoryItem(
                            entry = entry,
                            onDelete = {
                                waterViewModel.deleteWaterEntry(entry)
                            }
                        )
                    }
                }
            }

            // History (daily totals)
            Text(
                text = "Daily History",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (history.isEmpty()) {
                Text(
                    "No history yet",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(150.dp)
                ) {
                    items(history) { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(50, 50, 50), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(day.date, color = Color.White, fontSize = 14.sp)
                            Text(
                                String.format(
                                    Vocabulary.get().mlValue,
                                    day.consumedMl
                                ),
                                color = waterColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

//        // Floating notification button (placeholder)
//        FloatingActionButton(
//            onClick = { /* TODO */ },
//            containerColor = waterColor,
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Default.Notifications, contentDescription = Vocabulary.get().notifications)
//        }
    }

    // Dialog for editing cup sizes
    if (showEditCupDialog) {
        AlertDialog(
            onDismissRequest = { showEditCupDialog = false },
            title = { Text(Vocabulary.get().editCupSizesTitle, color = Color.White) },
            text = {
                Column {
                    Input(
                        String.format(Vocabulary.get().cupLabel, 1),
                        tempCup1,
                        onChange = { tempCup1 = it },
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(8.dp))
                    Input(
                        String.format(Vocabulary.get().cupLabel, 2),
                        tempCup2,
                        onChange = { tempCup2 = it },
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(8.dp))
                    Input(
                        String.format(Vocabulary.get().cupLabel, 3),
                        tempCup3,
                        onChange = { tempCup3 = it },
                        keyboardType = KeyboardType.Number
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val c1 = tempCup1.toIntOrNull() ?: cupSizes.first
                        val c2 = tempCup2.toIntOrNull() ?: cupSizes.second
                        val c3 = tempCup3.toIntOrNull() ?: cupSizes.third
                        cupSizes = Triple(c1, c2, c3)
                        showEditCupDialog = false
                    }
                ) {
                    Text(Vocabulary.get().save, color = waterColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCupDialog = false }) {
                    Text(Vocabulary.get().cancel, color = Color.Gray)
                }
            },
            containerColor = ContainerBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog for editing recommended intake
    if (showRecommendDialog) {
        AlertDialog(
            onDismissRequest = { showRecommendDialog = false },
            title = { Text(Vocabulary.get().setRecommendedIntake, color = Color.White) },
            text = {
                Input(
                    Vocabulary.get().recommendedMl,
                    tempIntake,
                    onChange = { tempIntake = it },
                    keyboardType = KeyboardType.Number
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tempIntake.toIntOrNull()?.let { recommendedIntake = it }
                        showRecommendDialog = false
                    }
                ) {
                    Text(Vocabulary.get().save, color = waterColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecommendDialog = false }) {
                    Text(Vocabulary.get().cancel, color = Color.Gray)
                }
            },
            containerColor = ContainerBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(Vocabulary.get().ok)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(Vocabulary.get().cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
}

@Composable
fun CupButton(amount: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = waterColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            String.format(Vocabulary.get().mlValue, amount),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HistoryItem(entry: WaterEntity, onDelete: () -> Unit) {   // ✅ changed type
    val timeString = java.text.SimpleDateFormat("HH:mm", LocalLocale.current.platformLocale)
        .format(entry.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(60, 60, 60), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                String.format(Vocabulary.get().mlValue, entry.amountMl),
                color = waterColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(timeString, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = Vocabulary.get().delete,
                tint = cancelColor
            )
        }
    }
}