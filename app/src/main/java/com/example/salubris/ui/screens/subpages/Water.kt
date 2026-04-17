package com.example.salubris.ui.screens.subpages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.entities.WaterEntry
import com.example.salubris.database.viewmodels.WaterViewModel
import com.example.salubris.database.viewmodels.waterViewModelFactory
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Water() {
    // Create ViewModel using the factory
    val factory = waterViewModelFactory(LocalContext.current)
    val waterViewModel: WaterViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val today = LocalDate.now().toString()

    // Tell ViewModel which date we are viewing
    LaunchedEffect(Unit) {
        waterViewModel.setDate(today)
    }

    // Observe data from ViewModel
    val entries by waterViewModel.todayEntries.collectAsState()
    val todayTotal by waterViewModel.todayTotal.collectAsState()
    val isLoading by waterViewModel.isLoading.collectAsState()

    // Cup sizes (editable)
    var cupSizes by remember { mutableStateOf(Triple(250, 500, 750)) }
    var showEditCupDialog by remember { mutableStateOf(false) }

    // Recommended intake
    var recommendedIntake by remember { mutableStateOf(2000) }
    var showRecommendDialog by remember { mutableStateOf(false) }

    // Temporary state for dialogs
    var tempCup1 by remember { mutableStateOf("") }
    var tempCup2 by remember { mutableStateOf("") }
    var tempCup3 by remember { mutableStateOf("") }
    var tempIntake by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    Text("Total Water Intake", color = Color.White, fontSize = 18.sp)
                    Text(
                        text = "$todayTotal ml",
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
                    waterViewModel.addWaterEntry(cupSizes.first, today)
                }
                CupButton(amount = cupSizes.second) {
                    waterViewModel.addWaterEntry(cupSizes.second, today)
                }
                CupButton(amount = cupSizes.third) {
                    waterViewModel.addWaterEntry(cupSizes.third, today)
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
                Text("Edit cup sizes", color = waterColor)
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
                        Text("Recommended Intake", color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$recommendedIntake ml",
                                color = waterColor,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                tempIntake = recommendedIntake.toString()
                                showRecommendDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = waterColor, modifier = Modifier.size(18.dp))
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
                        text = "${(todayTotal.toFloat() / recommendedIntake * 100).toInt()}% of daily goal",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Today's history list
            Text(
                text = "Today's History",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = waterColor)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
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
        }

        // Floating notification button (placeholder)
        FloatingActionButton(
            onClick = { /* TODO: Implement notification quick actions */ },
            containerColor = waterColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
        }
    }

    // Dialog for editing cup sizes
    if (showEditCupDialog) {
        AlertDialog(
            onDismissRequest = { showEditCupDialog = false },
            title = { Text("Edit Cup Sizes (ml)", color = Color.White) },
            text = {
                Column {
                    Input("Cup 1", tempCup1, onChange = { tempCup1 = it }, keyboardType = KeyboardType.Number)
                    Spacer(Modifier.height(8.dp))
                    Input("Cup 2", tempCup2, onChange = { tempCup2 = it }, keyboardType = KeyboardType.Number)
                    Spacer(Modifier.height(8.dp))
                    Input("Cup 3", tempCup3, onChange = { tempCup3 = it }, keyboardType = KeyboardType.Number)
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
                    Text("Save", color = waterColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCupDialog = false }) {
                    Text("Cancel", color = Color.Gray)
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
            title = { Text("Set Recommended Daily Intake (ml)", color = Color.White) },
            text = {
                Input("Recommended ml", tempIntake, onChange = { tempIntake = it }, keyboardType = KeyboardType.Number)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tempIntake.toIntOrNull()?.let { recommendedIntake = it }
                        showRecommendDialog = false
                    }
                ) {
                    Text("Save", color = waterColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecommendDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = ContainerBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CupButton(amount: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = waterColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("$amount ml", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HistoryItem(entry: WaterEntry, onDelete: () -> Unit) {
    val timeString = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
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
            Text("${entry.amountMl} ml", color = waterColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(timeString, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = cancelColor)
        }
    }
}