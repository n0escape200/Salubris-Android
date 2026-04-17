package com.example.salubris

import HomeTabsScreen
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.work.*
import com.example.salubris.database.AppDatabase
import com.example.salubris.stepcounter.StepService
import com.example.salubris.ui.components.Footer
import com.example.salubris.utils.WaterResetWorker
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Launcher for multiple permissions (Activity Recognition + Notifications)
    private val multiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d(TAG, "All required permissions granted, starting step service")
            startStepService()
        } else {
            Log.w(TAG, "Not all permissions granted. Activity recognition: ${permissions[Manifest.permission.ACTIVITY_RECOGNITION]}, Notifications: ${permissions[Manifest.permission.POST_NOTIFICATIONS]}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database (if needed)
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "Salubris").build()

        setContent {
            val pagerState = rememberPagerState(pageCount = { 5 })
            val currentPage = remember { mutableStateOf("Home") }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                requestNeededPermissions()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF232323), Color(0xFF121212))
                        )
                    )
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        Footer(
                            currentPage = currentPage.value,
                            onItemSelected = { label ->
                                val page = when (label) {
                                    "Home" -> 0
                                    "Tracking" -> 1
                                    "Products" -> 2
                                    "Meals" -> 3
                                    "Settings" -> 4
                                    else -> 0
                                }
                                coroutineScope.launch { pagerState.animateScrollToPage(page) }
                            }
                        )
                    }
                ) { innerPadding ->
                    HomeTabsScreen(
                        pagerState = pagerState,
                        currentPage = currentPage,
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 15.dp)
                    )
                }
            }
        }
    }

    private fun requestNeededPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Activity recognition (required for step counter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // Notification permission (required for Android 13+ to show foreground notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: $permissionsToRequest")
            multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // Permissions already granted
            Log.d(TAG, "Permissions already granted, starting step service")
            startStepService()
        }
    }

    private fun startStepService() {
        val intent = Intent(this, StepService::class.java)
        ContextCompat.startForegroundService(this, intent)

        // On Android 12+, ensure exact alarm permission is granted for midnight reset
        requestExactAlarmPermissionIfNeeded()

        // Request exemption from battery optimisation (critical for Motorola devices)
        requestIgnoreBatteryOptimizations()

        // Schedule midnight water reset using WorkManager
        scheduleWaterResetWork()
    }

    /**
     * For Android 12 (API 31) and above, SCHEDULE_EXACT_ALARM is a special permission
     * that must be granted by the user via system settings.
     */
    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "Exact alarm permission not granted. Opening system settings.")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } else {
                Log.d(TAG, "Exact alarm permission already granted.")
            }
        }
    }

    /**
     * Request the user to disable battery optimisation for this app.
     * This prevents Motorola (and other manufacturers) from killing the
     * step counter service when the app is in the background.
     */
    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.d(TAG, "Requesting battery optimisation exemption.")
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } else {
                Log.d(TAG, "Already exempt from battery optimisation.")
            }
        }
    }

    /**
     * Schedules a periodic WorkManager task to reset water intake at midnight every day.
     * The worker will run once per day, with an initial delay until the next midnight.
     */
    private fun scheduleWaterResetWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val resetRequest = PeriodicWorkRequestBuilder<WaterResetWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(calculateDelayUntilMidnight(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "water_reset",
            ExistingPeriodicWorkPolicy.KEEP,
            resetRequest
        )

        Log.d(TAG, "Water reset work scheduled with initial delay: ${calculateDelayUntilMidnight()} ms")
    }

    /**
     * Calculates milliseconds from now until the next midnight.
     */
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