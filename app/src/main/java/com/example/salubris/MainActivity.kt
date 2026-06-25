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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import androidx.work.*
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.DailyWaterHistoryEntity
import com.example.salubris.database.repositories.*
import com.example.salubris.database.viewmodels.SettingViewModel
import com.example.salubris.database.viewmodels.settingsViewModelFactory
import com.example.salubris.stepcounter.StepService
import com.example.salubris.ui.components.*
import com.example.salubris.ui.screens.dialogs.HealthReportDialog
import com.example.salubris.utils.FavoritesManager
import com.example.salubris.utils.Vocabulary
import com.example.salubris.utils.WaterResetWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.salubris.nutrition.MultiSourceWebScraper
import com.example.salubris.BuildConfig

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var database: AppDatabase

    // Permissions
    private val requiredPermissions = mutableListOf(
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.FOREGROUND_SERVICE_HEALTH
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private fun hasAllPermissions(): Boolean =
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private val permissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (hasAllPermissions()) {
                showAppContent()
                startStepService()
                requestExactAlarmPermissionIfNeeded()
                requestIgnoreBatteryOptimizations()
            } else {
                permissionsLauncher.launch(requiredPermissions)
            }
        }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "Salubris"
        ).build()

        if (hasAllPermissions()) {
            showAppContent()
            startStepService()
            requestExactAlarmPermissionIfNeeded()
            requestIgnoreBatteryOptimizations()
        } else {
            setContent {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            Vocabulary.get().permissionsRequiredTitle,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            Vocabulary.get().permissionsRequiredDescription,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { permissionsLauncher.launch(requiredPermissions) }) {
                            Text(Vocabulary.get().grantPermissions)
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // App content
    // ------------------------------------------------------------------
    private fun showAppContent() {
        setContent {
            val settingViewModel: SettingViewModel = viewModel(
                factory = settingsViewModelFactory(applicationContext)
            )
            val settings by settingViewModel.settings.collectAsState()
            val settingsMap = remember(settings) { settings.associate { it.name to it.value } }
            val savedLanguage = settingsMap["language"] ?: "en"

            LaunchedEffect(Unit) {
                Vocabulary.setLanguage(savedLanguage)
            }

            val language by Vocabulary.currentLanguage.collectAsState()

            // --- Page state ---
            val favoritesManager = remember { FavoritesManager(applicationContext) }
            val favorites by favoritesManager.favorites.collectAsState()
            val pagerState = rememberPagerState(pageCount = { favorites.size })
            val currentPage = remember { mutableStateOf(favorites.getOrNull(0) ?: "Home") }

            var overridePage by remember { mutableStateOf<String?>(null) }
            var navigateToPage by remember { mutableStateOf<String?>(null) }

            var showChatDialog by remember { mutableStateOf(false) }
            var modelPath by remember { mutableStateOf<String?>(null) }

            var showHealthReportDialog by remember { mutableStateOf(false) }

            val engine = remember { AiChat.getInferenceEngine(applicationContext) }

            LaunchedEffect(Unit) {
                val state = engine.state.value
                val modelFile = File(filesDir, "models/Qwen3.5-0.8B-Q8_0.gguf")

                if (state is InferenceEngine.State.ModelReady) {
                    modelPath = modelFile.absolutePath
                } else {
                    val path = copyModel("Qwen3.5-0.8B-Q8_0.gguf")
                    engine.loadModel(path)
                    modelPath = path
                }
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

            key(language) {
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
                                onOpenHealthReport = { showHealthReportDialog = true },
                                actions = listOf(
                                    FooterAction(
                                        icon = Icons.Default.Chat,
                                        contentDescription = Vocabulary.get().aiAssistant,
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
                            webSearchService = MultiSourceWebScraper(usdaApiKey = BuildConfig.USDA_API_KEY),
                            onDismiss = { showChatDialog = false }
                        )
                    }

                    if (showHealthReportDialog) {
                        HealthReportDialog(
                            generatePayload = { generateHealthReportJson() },
                            onDismiss = { showHealthReportDialog = false },
                            onSuccess = { response ->
                                Log.d(TAG, "Report submitted successfully: $response")
                            }
                        )
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Health Report JSON generation
    // ------------------------------------------------------------------
    private suspend fun generateHealthReportJson(): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting health report generation...")
        try {
            val macroRepo = MacroRepository(database.macroDao())
            val settingRepo = SettingRepository(database.settingDao())
            val waterRepo = WaterRepository(database.waterDao())
            val stepRepo = StepHistoryRepository(database.stepHistoryDao())

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()

            // 1. Profile
            val settingsList = try {
                settingRepo.getAllSettings().first()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch settings: ${e.message}")
                emptyList()
            }
            val profileMap = settingsList.associate { it.name to it.value }
            Log.d(TAG, "Profile settings: $profileMap")

            val profile = JSONObject().apply {
                put("name", profileMap["user_name"] ?: "")
                put("age", profileMap["user_age"]?.toIntOrNull() ?: 0)
                put("sex", profileMap["user_sex"] ?: "")
                put("heightCm", profileMap["user_height_cm"]?.toFloatOrNull() ?: 0f)
                put("weightKg", profileMap["user_weight_kg"]?.toFloatOrNull() ?: 0f)
                put("activityLevel", profileMap["user_activity_level"] ?: "")
                put("goal", profileMap["user_goal"] ?: "")
                put("calorieGoal", profileMap["recommended_calories"]?.toIntOrNull() ?: 0)
            }

            // 2. Last 7 days
            val daysArray = JSONArray()
            for (i in 6 downTo 0) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val dateStr = dateFormat.format(calendar.time)

                val startOfDay = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // Macros
                val macros = macroRepo.getMacrosForDay(startOfDay)
                var totalCalories = 0f
                var totalProtein = 0f
                var totalCarbs = 0f
                var totalFats = 0f
                macros.forEach {
                    totalCalories += it.calories
                    totalProtein += it.protein
                    totalCarbs += it.carbs
                    totalFats += it.fats
                }
                Log.d(
                    TAG,
                    "Day $dateStr macros: cals=$totalCalories, protein=$totalProtein, carbs=$totalCarbs, fats=$totalFats"
                )

                // Water
                val waterResult = waterRepo.getTodayTotal(dateStr).firstOrNull()
                Log.d(
                    TAG,
                    "Water result for $dateStr: $waterResult (type: ${waterResult?.javaClass?.simpleName})"
                )
                val waterMl = when (waterResult) {
                    is DailyWaterHistoryEntity -> waterResult.consumedMl
                    is Int -> waterResult
                    else -> 0
                }
                Log.d(TAG, "Day $dateStr water: $waterMl ml")

                // Steps
                val allSteps = stepRepo.getHistoryPaged(limit = 10, offset = 0).first()
                val stepsForDay = allSteps.find { it.date == dateStr }?.steps ?: 0
                Log.d(TAG, "Day $dateStr steps: $stepsForDay")

                val dayJson = JSONObject().apply {
                    put("date", dateStr)
                    put("macros", JSONObject().apply {
                        put("calories", totalCalories)
                        put("protein", totalProtein)
                        put("carbs", totalCarbs)
                        put("fats", totalFats)
                    })
                    put("waterMl", waterMl)
                    put("steps", stepsForDay)
                }
                daysArray.put(dayJson)
            }

            // Get current language
            val language = Vocabulary.currentLanguage.value

            // Build final JSON with language field
            val json = JSONObject().apply {
                put("language", language) // ✅ Add language field
                put("profile", profile)
                put("days", daysArray)
            }.toString(2)

            Log.d(TAG, "Generated JSON:\n$json")
            return@withContext json

        } catch (e: Exception) {
            Log.e(TAG, "Error generating health report", e)
            throw e
        }
    }

    // ------------------------------------------------------------------
    // Helper methods (unchanged)
    // ------------------------------------------------------------------
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

    private fun startStepService() {
        if (!hasAllPermissions()) return

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