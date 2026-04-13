package com.example.salubris.ui.screens.subpages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salubris.stepcounter.StepRepository

@Composable
fun Steps() {

    val steps by StepRepository.steps.collectAsState()

    val dailyGoal = 10000f
    val progress = (steps / dailyGoal).coerceAtMost(1f)

    val remaining = (dailyGoal - steps).coerceAtLeast(0f).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(25, 25, 25))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // HEADER
        Text(
            text = "Step Tracker",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        // MAIN CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(45, 45, 45), RoundedCornerShape(16.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "$steps steps",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Goal: ${dailyGoal.toInt()} steps",
                color = Color(180, 180, 180),
                fontSize = 14.sp
            )

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .padding(top = 8.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(80, 80, 80)
            )

            Text(
                text = "${(progress * 100).toInt()}% completed",
                color = Color(200, 200, 200),
                fontSize = 14.sp
            )
        }

        // INSIGHTS CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(45, 45, 45), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "Insights",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (steps < 3000)
                    "Low activity — try taking a short walk."
                else if (steps < 7000)
                    "Good progress — you're getting active."
                else if (steps < 10000)
                    "Great job — almost at your goal!"
                else
                    "Excellent — goal achieved 🎉",
                color = Color(180, 180, 180),
                fontSize = 14.sp
            )

            Text(
                text = "Remaining: $remaining steps",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // PLACEHOLDER FOR HISTORY (future-ready)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(45, 45, 45), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {

            Text(
                text = "History",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Daily tracking history will appear here once persistence is added.",
                color = Color(150, 150, 150),
                fontSize = 13.sp
            )
        }
    }
}