package com.example.salubris

import HomeTabsScreen
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.work.*
import com.example.salubris.database.AppDatabase
import com.example.salubris.stepcounter.StepService
import com.example.salubris.ui.components.*
import com.example.salubris.utils.FavoritesManager
import com.example.salubris.utils.WaterResetWorker
import com.example.salubris.utils.buildQwenPrompt
import com.example.salubris.utils.copyModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val multiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) startStepService()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "Salubris"
        ).build()

        setContent {
            val favoritesManager = remember { FavoritesManager(applicationContext) }
            val favorites by favoritesManager.favorites.collectAsState()

            val pagerState = rememberPagerState(pageCount = { favorites.size })
            val currentPage = remember { mutableStateOf(favorites.getOrNull(0) ?: "Home") }
            var overridePage by remember { mutableStateOf<String?>(null) }
            var navigateToPage by remember { mutableStateOf<String?>(null) }

            var showChatDialog by remember { mutableStateOf(false) }

            var isModelReady by remember { mutableStateOf(false) }

            // Initialize LLM *before* it is used
            val llama = remember { LlamaChatHelper(applicationContext.contentResolver) }

            LaunchedEffect(Unit) {
                val modelPath = copyModel(applicationContext, "qwen2.5-1.5b-instruct-q4_k_m.gguf")
                llama.loadModel(modelPath)
                isModelReady = true
            }

            // Handle navigation requests via LaunchedEffect
            LaunchedEffect(navigateToPage) {
                navigateToPage?.let { page ->
                    if (favorites.contains(page)) {
                        val index = favorites.indexOf(page)
                        pagerState.animateScrollToPage(index)
                        currentPage.value = page
                        overridePage = null
                    } else {
                        overridePage = page
                        currentPage.value = page
                    }
                    navigateToPage = null
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF232323), Color(0xFF121212))
                        )
                    )
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        Footer(
                            currentPage = currentPage.value,
                            favorites = favorites,
                            onItemSelected = { label ->
                                navigateToPage = label
                            },
                            onUpdateFavorites = { newFavorites ->
                                favoritesManager.saveFavorites(newFavorites)
                            },
                            actions = listOf(
                                FooterAction(
                                    icon = Icons.Default.Chat,
                                    contentDescription = "AI Assistant",
                                    onClick = { showChatDialog = true }
                                )
                            )
                        )
                    }
                ) { innerPadding ->
                    HomeTabsScreen(
                        favorites = favorites,
                        pagerState = pagerState,
                        currentPage = currentPage,
                        overridePage = overridePage,
                        onCloseOverride = { overridePage = null },
                        onNavigateToPage = { page ->
                            navigateToPage = page
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                if (showChatDialog) {
                    ChatDialog(
                        isOpen = showChatDialog,
                        onClose = { showChatDialog = false },
                        onSend = { prompt ->
                            if (!isModelReady) "Model not ready yet. Please try again."
                            else llama.generate(buildQwenPrompt(prompt))
                        }
                    )
                }
            }
        }
    }

    private fun startStepService() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) return

        val intent = Intent(this, StepService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
            }
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
            }
        }
    }

    private fun scheduleWaterResetWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val resetRequest = PeriodicWorkRequestBuilder<WaterResetWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateDelayUntilMidnight(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "water_reset",
            ExistingPeriodicWorkPolicy.KEEP,
            resetRequest
        )
    }

    private fun calculateDelayUntilMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }
        return midnight.timeInMillis - now.timeInMillis
    }
}