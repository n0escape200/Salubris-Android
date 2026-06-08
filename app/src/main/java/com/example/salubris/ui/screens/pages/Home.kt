package com.example.salubris.ui.screens.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.viewmodels.*
import com.example.salubris.database.viewmodels.macroViewModelFactory
import com.example.salubris.database.viewmodels.settingsViewModelFactory
import com.example.salubris.database.viewmodels.waterViewModelFactory
import com.example.salubris.stepcounter.StepRepository
import com.example.salubris.ui.theme.*
import com.example.salubris.utils.truncate2Decimals
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

fun calculateTotalCalories(list: List<TrackedItem>): Float {
    return list.fold(0f) { total, item -> total + item.calories }
}

@Composable
fun Header(userName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = ContainerBackground, shape = MainContainerBorder)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row {
            Box(
                modifier = Modifier
                    .border(1.dp, Color(77, 184, 255), shape = RoundedCornerShape(50))
                    .padding(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User icon",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)) {
                Text("Welcome back", color = Color.White)
                Text(if (userName.isBlank()) "User" else userName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun StepCard(stepGoal: Int) {
    val steps by StepRepository.steps.collectAsState()
    val sensorAvailable by StepRepository.sensorAvailable.collectAsState()
    val progress = if (sensorAvailable) (steps.toFloat() / stepGoal).coerceAtMost(1f) else 0f
    val remaining = if (sensorAvailable) (stepGoal - steps).coerceAtLeast(0) else 0

    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        colors = CardDefaults.cardColors(containerColor = ContainerBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DirectionsWalk,
                contentDescription = "Steps",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Steps", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (sensorAvailable) {
                    Text("$steps / $stepGoal", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.DarkGray
                    )
                    Text(
                        "${(progress * 100).toInt()}% • $remaining steps remaining",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        "Step sensor not available",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WaterCard(waterGoal: Int, waterViewModel: WaterViewModel) {
    val todayTotal by waterViewModel.todayTotal.collectAsState()
    val progress = (todayTotal.toFloat() / waterGoal).coerceAtMost(1f)
    val remaining = (waterGoal - todayTotal).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = ContainerBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.WaterDrop,
                contentDescription = "Water",
                tint = waterColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("Water", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("$todayTotal / $waterGoal ml", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(top = 4.dp),
                    color = waterColor,
                    trackColor = Color.DarkGray
                )
                Text(
                    "${(progress * 100).toInt()}% • $remaining ml remaining",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodayIntake(
    macroViewModel: MacroViewModel,
    goalCalories: Int,
    goalType: String,
    refreshTrigger: Boolean
) {
    var trackedItems by remember { mutableStateOf<List<TrackedItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        val todayMillis = LocalDate.now(ZoneOffset.UTC)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        trackedItems = withContext(Dispatchers.IO) {
            macroViewModel.getTrackedItemsForDay(todayMillis)
        }
        isLoading = false
    }

    val totalCalories = calculateTotalCalories(trackedItems)
    val remaining = goalCalories - totalCalories
    val isLoss = goalType.contains("LOSS")
    val isGain = goalType.contains("GAIN")
    val isMaintain = goalType == "MAINTAIN"
    val progressPercentage = if (goalCalories > 0) (totalCalories / goalCalories) * 100 else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = ContainerBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = "Calories",
                    tint = caloriesColor,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Today's Intake",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                if (isLoading) {
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${totalCalories.truncate2Decimals()} / $goalCalories",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            Text("calories", color = Color.LightGray)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (totalCalories / goalCalories).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = caloriesColor,
                trackColor = Color.DarkGray
            )
            Spacer(Modifier.height(8.dp))

            when {
                isLoss -> {
                    if (remaining > 0) {
                        Text(
                            text = "🎯 $remaining calories left",
                            color = submitColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Progress: ${"%.1f".format(progressPercentage)}%",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "⚠️ You've exceeded your goal",
                            color = Color(0xFFFF9800),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                isGain -> {
                    if (remaining > 0) {
                        Text(
                            text = "💪 $remaining more calories needed",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Progress: ${"%.1f".format(progressPercentage)}%",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "✅ Goal achieved!",
                            color = submitColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                isMaintain -> {
                    if (remaining > 0) {
                        Text(
                            text = "$remaining calories remaining",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    } else if (remaining < 0) {
                        Text(
                            text = "Exceeded by ${-remaining} calories",
                            color = Color(0xFFFF9800),
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = "Perfect!",
                            color = submitColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(weeklyData: List<Float>, modifier: Modifier = Modifier) {
    if (weeklyData.all { it == 0f }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
                .border(1.dp, Color(0xFF4DB8FF), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No data this week", color = Color.LightGray, fontSize = 14.sp)
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E))
            .border(1.dp, Color(0xFF4DB8FF), RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                LineChart(context).apply {
                    val entries = weeklyData.mapIndexed { index, value ->
                        Entry(index.toFloat(), value)
                    }
                    val dataSet = LineDataSet(entries, "Calories").apply {
                        lineWidth = 3f
                        setDrawCircles(true)
                        circleRadius = 5f
                        setDrawCircleHole(false)
                        color = android.graphics.Color.parseColor("#4DB8FF")
                        setCircleColor(android.graphics.Color.WHITE)
                        setDrawValues(true)
                        valueTextSize = 12f
                        valueTextColor = android.graphics.Color.WHITE
                        setDrawFilled(true)
                        fillColor = android.graphics.Color.parseColor("#4DB8FF")
                        fillAlpha = 50
                    }
                    data = LineData(dataSet)
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        textColor = android.graphics.Color.WHITE
                        textSize = 12f
                        granularity = 1f
                        valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                            private val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            override fun getFormattedValue(value: Float): String {
                                val index = value.toInt()
                                return if (index in days.indices) days[index] else ""
                            }
                        }
                    }
                    axisLeft.apply {
                        setDrawGridLines(true)
                        textColor = android.graphics.Color.WHITE
                        textSize = 12f
                        axisMinimum = 0f
                    }
                    axisRight.isEnabled = false
                    setExtraOffsets(16f, 16f, 16f, 16f)
                    description.isEnabled = false
                    legend.isEnabled = false
                    setTouchEnabled(false)
                    invalidate()
                }
            }
        )
    }
}

@Composable
fun Analytics(weeklyCalories: List<Float>, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = ContainerBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Analytics",
                    tint = Color(0, 255, 102),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    "Analytics",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
            }
            Text("Weekly caloric intake", color = Color.White, fontWeight = FontWeight.W500)
            SimpleLineChart(weeklyCalories)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(
    macroViewModel: MacroViewModel = viewModel(factory = macroViewModelFactory(LocalContext.current)),
    settingViewModel: SettingViewModel = viewModel(factory = settingsViewModelFactory(LocalContext.current)),
    waterViewModel: WaterViewModel = viewModel(factory = waterViewModelFactory(LocalContext.current))
) {
    val settings by settingViewModel.settings.collectAsStateWithLifecycle()
    val settingsMap = remember(settings) { settings.associate { it.name to it.value } }
    val userName = settingsMap["user_name"] ?: "User"
    val goalCalories = settingsMap["recommended_calories"]?.toIntOrNull() ?: 2000
    val goalType = settingsMap["user_goal"] ?: "MAINTAIN"
    val goalSteps = settingsMap["goal_steps"]?.toIntOrNull() ?: 10000
    val goalWater = settingsMap["goal_water"]?.toIntOrNull() ?: 2000

    var weeklyCalories by remember { mutableStateOf<List<Float>>(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)) }
    var refreshTrigger by remember { mutableStateOf(false) }

    val today = LocalDate.now().toString()
    LaunchedEffect(Unit) {
        waterViewModel.setDate(today)
    }

    suspend fun loadWeeklyData() {
        withContext(Dispatchers.IO) {
            val today = LocalDate.now(ZoneOffset.UTC)
            val monday = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            val days = (0..6).map { monday.plusDays(it.toLong()) }
            val results = mutableListOf<Float>()
            for (date in days) {
                val startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                val items = macroViewModel.getTrackedItemsForDay(startOfDay)
                val totalCal = items.sumOf { it.calories.toDouble() }.toFloat()
                results.add(totalCal)
            }
            weeklyCalories = results
        }
    }

    LaunchedEffect(refreshTrigger) {
        loadWeeklyData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Header(userName = userName)
        TodayIntake(
            macroViewModel = macroViewModel,
            goalCalories = goalCalories,
            goalType = goalType,
            refreshTrigger = refreshTrigger
        )
        Analytics(weeklyCalories = weeklyCalories, onRefresh = { refreshTrigger = !refreshTrigger })
        StepCard(stepGoal = goalSteps)
        WaterCard(waterGoal = goalWater, waterViewModel = waterViewModel)
    }
}