package com.example.salubris.ui.screens.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.repositories.StepRepository
import com.example.salubris.database.viewmodels.MacroViewModel
import com.example.salubris.database.viewmodels.SettingViewModel
import com.example.salubris.database.viewmodels.TrackedItem
import com.example.salubris.database.viewmodels.WaterViewModel
import com.example.salubris.database.viewmodels.macroViewModelFactory
import com.example.salubris.database.viewmodels.settingsViewModelFactory
import com.example.salubris.database.viewmodels.waterViewModelFactory
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.MainContainerBorder
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.ui.theme.productColor
import com.example.salubris.ui.theme.submitColor
import com.example.salubris.ui.theme.waterColor
import com.example.salubris.utils.Vocabulary
import com.example.salubris.utils.truncate2Decimals
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

fun calculateTotalCalories(list: List<TrackedItem>): Float {
    return list.fold(0f) { total, item -> total + item.calories * (item.amountOrMultiplier / 100) }
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
                    contentDescription = Vocabulary.get().userIcon,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)) {
                Text(Vocabulary.get().welcomeBack, color = Color.White)
                Text(
                    if (userName.isBlank()) Vocabulary.get().userDefault else userName,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
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
                Icons.Default.DirectionsWalk,
                contentDescription = Vocabulary.get().stepsIcon,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    Vocabulary.get().steps,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (sensorAvailable) {
                    Text(
                        "$steps / $stepGoal",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(top = 4.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.DarkGray
                    )
                    Text(
                        "${(progress * 100).toInt()}% • $remaining ${Vocabulary.get().remainingSteps}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        Vocabulary.get().stepSensorNotAvailable,
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
                contentDescription = Vocabulary.get().waterIcon,
                tint = waterColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    Vocabulary.get().water,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "$todayTotal / $waterGoal ml",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
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
                    "${(progress * 100).toInt()}% • $remaining ${Vocabulary.get().mlRemaining}",
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
                    contentDescription = Vocabulary.get().caloriesIcon,
                    tint = caloriesColor,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    Vocabulary.get().todaysIntake,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                if (isLoading) {
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${totalCalories.truncate2Decimals()} / $goalCalories",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            Text(Vocabulary.get().calories, color = Color.LightGray)
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
                            text = String.format(Vocabulary.get().caloriesLeft, remaining.toInt()),
                            color = submitColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = String.format(
                                Vocabulary.get().progressPercent,
                                progressPercentage
                            ),
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = Vocabulary.get().exceededGoal,
                            color = Color(0xFFFF9800),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                isGain -> {
                    if (remaining > 0) {
                        Text(
                            text = String.format(
                                Vocabulary.get().moreCaloriesNeeded,
                                remaining.toInt()
                            ),
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = String.format(
                                Vocabulary.get().progressPercent,
                                progressPercentage
                            ),
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = Vocabulary.get().goalAchieved,
                            color = submitColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                isMaintain -> {
                    if (remaining > 0) {
                        Text(
                            text = String.format(
                                Vocabulary.get().caloriesRemaining,
                                remaining.toInt()
                            ),
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    } else if (remaining < 0) {
                        Text(
                            text = String.format(Vocabulary.get().exceededBy, (-remaining).toInt()),
                            color = Color(0xFFFF9800),
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = Vocabulary.get().perfect,
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
            Text(Vocabulary.get().noDataThisWeek, color = Color.LightGray, fontSize = 14.sp)
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
                    val dataSet = LineDataSet(entries, Vocabulary.get().calories).apply {
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
                        valueFormatter =
                            object : com.github.mikephil.charting.formatter.ValueFormatter() {
                                private val days =
                                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

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
                    contentDescription = Vocabulary.get().analyticsIcon,
                    tint = Color(0, 255, 102),
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    Vocabulary.get().analytics,
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = Vocabulary.get().refreshIcon,
                        tint = Color.White
                    )
                }
            }
            Text(
                Vocabulary.get().weeklyCaloricIntake,
                color = Color.White,
                fontWeight = FontWeight.W500
            )
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
    val userName = settingsMap["user_name"] ?: Vocabulary.get().userDefault
    val goalCalories = settingsMap["recommended_calories"]?.toIntOrNull() ?: 2000
    val goalType = settingsMap["user_goal"] ?: "MAINTAIN"
    val goalSteps = settingsMap["goal_steps"]?.toIntOrNull() ?: 10000
    val goalWater = settingsMap["goal_water"]?.toIntOrNull() ?: 2000

    var weeklyCalories by remember {
        mutableStateOf<List<Float>>(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f))
    }
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
                val totalCal =
                    items.sumOf { (it.calories * it.amountOrMultiplier / 100).toDouble() }.toFloat()
                results.add(totalCal)
            }
            weeklyCalories = results
        }
    }

    LaunchedEffect(refreshTrigger) {
        loadWeeklyData()
    }

    // --- Loading state: wait for sensor or timeout (2 seconds) ---
    var isLoading by remember { mutableStateOf(true) }
    val sensorAvailable by StepRepository.sensorAvailable.collectAsState()

    LaunchedEffect(sensorAvailable) {
        if (sensorAvailable) {
            isLoading = false
        }
    }

    // Fallback: hide loading after 2 seconds even if sensor not ready
    LaunchedEffect(Unit) {
        delay(2000)
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = productColor)
        }
        return
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
        Analytics(
            weeklyCalories = weeklyCalories,
            onRefresh = { refreshTrigger = !refreshTrigger }
        )
        StepCard(stepGoal = goalSteps)
        WaterCard(waterGoal = goalWater, waterViewModel = waterViewModel)
    }
}