package com.example.salubris.ui.screens.pages

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.viewmodels.MacroViewModel
import com.example.salubris.database.viewmodels.TrackedItem
import com.example.salubris.database.viewmodels.macroViewModelFactory
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.MainContainerBorder
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.utils.truncate2Decimals
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

fun calculateTotalCalories(list: List<TrackedItem>): Float {
    return list.fold(0f) { total, item -> total + item.calories }
}

@Composable
fun Header() {
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
                Text("User", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodayIntake(macroViewModel: MacroViewModel) {
    var trackedItems by remember { mutableStateOf<List<TrackedItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        val todayMillis = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        trackedItems = macroViewModel.getTrackedItemsForDay(todayMillis)
        Log.d("TodayIntake", "Today's calories: ${calculateTotalCalories(trackedItems)}")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ContainerBackground, shape = MainContainerBorder)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Flame",
                tint = caloriesColor,
                modifier = Modifier.size(30.dp)
            )
            Text(
                "Today's intake",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                modifier = Modifier.padding(10.dp, 0.dp, 0.dp, 0.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${calculateTotalCalories(trackedItems).truncate2Decimals()}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )
            Text("calories", color = Color.LightGray)
        }
    }
}

@Composable
fun SimpleLineChart(weeklyData: List<Float>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
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
fun Analytics(weeklyCalories: List<Float>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ContainerBackground, shape = MainContainerBorder)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = "Analytics",
                tint = Color(0, 255, 102),
                modifier = Modifier.size(30.dp)
            )
            Text(
                "Analytics",
                modifier = Modifier.padding(10.dp, 0.dp, 0.dp, 0.dp),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column {
            Text("Weekly caloric intake", color = Color.White, fontWeight = FontWeight.W500)
            Spacer(modifier = Modifier.height(10.dp))
            SimpleLineChart(weeklyCalories)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Home(macroViewModel: MacroViewModel = viewModel(factory = macroViewModelFactory(LocalContext.current))) {
    var weeklyCalories by remember { mutableStateOf<List<Float>>(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val today = LocalDate.now()
            val monday = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            val days = (0..6).map { monday.plusDays(it.toLong()) }
            val zone = ZoneId.systemDefault()
            val results = mutableListOf<Float>()
            for (date in days) {
                val startOfDay = date.atStartOfDay(zone).toInstant().toEpochMilli()
                val items = macroViewModel.getTrackedItemsForDay(startOfDay)
                val totalCal = items.sumOf { it.calories.toDouble() }.toFloat()
                Log.d("WeeklyChart", "Date: $date, startOfDay: $startOfDay, totalCal: $totalCal")
                results.add(totalCal)
            }
            weeklyCalories = results
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header()
        TodayIntake(macroViewModel)
        Analytics(weeklyCalories)
    }
}