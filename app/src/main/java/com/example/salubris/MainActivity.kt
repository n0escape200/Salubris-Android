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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.work.*
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.example.salubris.database.AppDatabase
import com.example.salubris.stepcounter.StepService
import com.example.salubris.ui.components.*
import com.example.salubris.utils.FavoritesManager
import com.example.salubris.utils.WaterResetWorker
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    // Required permissions
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.FOREGROUND_SERVICE_HEALTH
    )

    private fun hasAllPermissions(): Boolean =
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    // Explicit type breaks circular inference
    private val permissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (hasAllPermissions()) {
                showAppContent()
                startStepService()
            } else {
                // Loop until granted
                permissionsLauncher.launch(requiredPermissions)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "Salubris"
        ).build()

        // Show permission request or the real UI
        if (hasAllPermissions()) {
            showAppContent()
            startStepService()
        } else {
            // Embedded permission UI – no separate composable function
            setContent {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Permissions Required", color = Color.White, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "This app needs activity recognition and health service permissions to count your steps.",
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { permissionsLauncher.launch(requiredPermissions) }) {
                            Text("Grant Permissions")
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Main app content
    // -------------------------------------------------------------------------
    private fun showAppContent() {
        setContent {
            val favoritesManager = remember { FavoritesManager(applicationContext) }
            val favorites by favoritesManager.favorites.collectAsState()

            val pagerState = rememberPagerState(pageCount = { favorites.size })
            val currentPage = remember { mutableStateOf(favorites.getOrNull(0) ?: "Home") }

            var overridePage by remember { mutableStateOf<String?>(null) }
            var navigateToPage by remember { mutableStateOf<String?>(null) }

            var showChatDialog by remember { mutableStateOf(false) }
            var modelPath by remember { mutableStateOf<String?>(null) }

            val engine = remember { AiChat.getInferenceEngine(applicationContext) }

            // Copy model from assets and load it
            LaunchedEffect(Unit) {
                val path = copyModel("Qwen3.5-0.8B-Q8_0.gguf")
                engine.loadModel(path)
                modelPath = path
            }

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
                            onItemSelected = { label -> navigateToPage = label },
                            onUpdateFavorites = { newFavorites ->
                                favoritesManager.saveFavorites(newFavorites)
                            },
                            onOpenChat = { showChatDialog = true },
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
                        onNavigateToPage = { page -> navigateToPage = page },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                if (showChatDialog && modelPath != null) {
                    ChatDialog(
                        engine = engine,
                        modelPath = modelPath!!,
                        onDismiss = { showChatDialog = false }
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Model helper
    // -------------------------------------------------------------------------
    private fun copyModel(assetFileName: String): String {
        val modelFile = File(filesDir, "models/$assetFileName")
        if (modelFile.exists()) return modelFile.absolutePath

        modelFile.parentFile?.mkdirs()
        assets.open(assetFileName).use { input ->
            modelFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Failed to copy model from assets")
        return modelFile.absolutePath
    }

    // -------------------------------------------------------------------------
    // Step service start
    // -------------------------------------------------------------------------
    private fun startStepService() {
        if (!hasAllPermissions()) return

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) return

        val intent = Intent(this, StepService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    // -------------------------------------------------------------------------
    // Permission helpers (unchanged from your original)
    // -------------------------------------------------------------------------
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