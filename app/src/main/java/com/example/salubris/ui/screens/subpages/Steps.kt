package com.example.salubris.ui.screens.subpages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.repositories.StepRepository
import com.example.salubris.database.viewmodels.StepHistoryViewModel
import com.example.salubris.database.viewmodels.stepHistoryViewModelFactory
import com.example.salubris.utils.Vocabulary

@Composable
fun Steps() {
    val context = LocalContext.current

    // StepHistoryViewModel for history
    val historyViewModel: StepHistoryViewModel = viewModel(
        factory = stepHistoryViewModelFactory(context)
    )

    // Current steps
    val steps by StepRepository.steps.collectAsState()
    val history by historyViewModel.history.collectAsState()
    val isLoadingHistory by historyViewModel.isLoading.collectAsState()
    val canLoadMore by historyViewModel.canLoadMore.collectAsState()

    // Load first page on start
    LaunchedEffect(Unit) {
        historyViewModel.refresh()
    }

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
            text = Vocabulary.get().stepTracker,
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
                text = String.format(Vocabulary.get().stepsCount, steps),
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = String.format(Vocabulary.get().goalSteps, dailyGoal.toInt()),
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
                text = String.format(Vocabulary.get().percentCompleted, (progress * 100).toInt()),
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
                text = Vocabulary.get().insights,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            val insightMessage = when {
                steps < 3000 -> Vocabulary.get().lowActivity
                steps < 7000 -> Vocabulary.get().goodProgress
                steps < 10000 -> Vocabulary.get().greatJob
                else -> Vocabulary.get().excellentGoalAchieved
            }
            Text(
                text = insightMessage,
                color = Color(180, 180, 180),
                fontSize = 14.sp
            )
            Text(
                text = String.format(Vocabulary.get().remainingStepsLabel, remaining),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // HISTORY SECTION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(45, 45, 45), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = Vocabulary.get().history,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (history.isEmpty() && !isLoadingHistory) {
                Text(
                    text = "No step history yet.",
                    color = Color(150, 150, 150),
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(history) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(60, 60, 60), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = entry.date,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${entry.steps} steps",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Load more button
                if (canLoadMore) {
                    Button(
                        onClick = { historyViewModel.loadNextPage() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoadingHistory
                    ) {
                        if (isLoadingHistory) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Load More", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}