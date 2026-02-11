package com.example.salubris.ui.screens.pages

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.MainContainerBorder
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.security.KeyStore
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.core.view.setPadding
import com.example.salubris.ui.theme.caloriesColor
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*


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
                    .border(
                        1.dp, Color(77, 184, 255), shape = RoundedCornerShape(50)
                    )
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

            Column(
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)
            ) {
                Text("Welcome back", color = Color.White)
                Text(
                    "User", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                )
            }
        }
    }
}


@Composable
fun TodayIntake() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ContainerBackground, shape = MainContainerBorder)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Text("317", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
            Text("calories", color = Color.LightGray)
        }
    }
}



@Composable
fun SimpleLineChart(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))       // Rounded corners
            .background(Color(0xFF1E1E1E))         // Chart background color
            .border(1.dp, Color(0xFF4DB8FF), RoundedCornerShape(16.dp)) // Border
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                LineChart(context).apply {

                    val entries = listOf(
                        Entry(0f, 3f),
                        Entry(1f, 5f),
                        Entry(2f, 2f),
                        Entry(3f, 8f),
                        Entry(4f, 4f)
                    )

                    val dataSet = LineDataSet(entries, "Sample Data").apply {
                        lineWidth = 3f
                        enableDashedLine(10f, 10f, 0f)
                        setDrawCircles(true)
                        circleRadius = 5f
                        setDrawCircleHole(false)
                        color = android.graphics.Color.parseColor("#4DB8FF")
                        setCircleColor(android.graphics.Color.WHITE)
                        setDrawValues(false)
                    }

                    data = LineData(dataSet)

                    // X axis
                    xAxis.apply {
                        position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        textColor = android.graphics.Color.WHITE
                        textSize = 16f
                        granularity = 1f
                        valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                            private val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            override fun getFormattedValue(value: Float): String {
                                return days.getOrNull(value.toInt()) ?: ""
                            }
                        }
                    }

                    // Left axis
                    axisLeft.apply {
                        setDrawGridLines(false)
                        textColor = android.graphics.Color.WHITE
                        textSize = 16f
                    }
                    axisRight.isEnabled = false

                    // Padding inside chart
                    setExtraOffsets(16f, 16f, 16f, 24f)

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
fun Analytics(){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ContainerBackground, shape = MainContainerBorder)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row{
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = "Analytics",
                tint = Color(0, 255, 102),
                modifier = Modifier.size(30.dp)
            )
            Text("Analytics",
                modifier = Modifier
                    .padding(10.dp,0.dp,0.dp,0.dp),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold)
        }
        Column {
            Text("Weekly caloric intake", color = Color.White, fontWeight = FontWeight.W500)
            Spacer(modifier = Modifier.height(10.dp))
            SimpleLineChart()
        }
    }
}

@Composable
fun Home(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header()
        TodayIntake()
        Analytics()
    }
}