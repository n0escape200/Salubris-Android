package com.example.salubris

import HomeTabsScreen
import android.Manifest
import android.annotation.SuppressLint
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
import com.example.salubris.utils.FavoritesManager
import com.example.salubris.utils.WaterResetWorker
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }



    private val multiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d(TAG, "All required permissions granted, starting step service")
            startStepService()
        } else {
            Log.w(TAG, "Not all permissions granted.")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "Salubris").build()

        setContent {
            val favoritesManager = remember { FavoritesManager(applicationContext) }
            val favorites by favoritesManager.favorites.collectAsState()
            val pagerState = rememberPagerState(pageCount = { favorites.size })
            val currentPage = remember { mutableStateOf(favorites.getOrNull(0) ?: "Home") }
            var overridePage by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()

            // Update currentPage when pager scrolls (if not in override)
            LaunchedEffect(pagerState.currentPage, overridePage) {
                if (overridePage == null) {
                    currentPage.value = favorites.getOrNull(pagerState.currentPage) ?: "Home"
                } else {
                    currentPage.value = overridePage!!
                }
            }

            // Adjust pager when favorites change (only if not in override)
            LaunchedEffect(favorites, overridePage) {
                if (overridePage == null) {
                    val newIndex = favorites.indexOf(currentPage.value).coerceAtLeast(0)
                    pagerState.scrollToPage(newIndex)
                }
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
                            favorites = favorites,
                            onItemSelected = { label ->
                                // This is called when user clicks a page in the FAB menu or footer
                                if (favorites.contains(label)) {
                                    // Favorite: close override if any, scroll to it
                                    if (overridePage != null) {
                                        overridePage = null
                                        // after closing, scroll
                                        val index = favorites.indexOf(label)
                                        if (index != -1) {
                                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                        }
                                    } else {
                                        // Already in pager mode, just scroll
                                        val index = favorites.indexOf(label)
                                        if (index != -1) {
                                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                        }
                                    }
                                } else {
                                    // Unfavorited: set override page
                                    overridePage = label
                                }
                            },
                            onUpdateFavorites = { newFavorites ->
                                favoritesManager.saveFavorites(newFavorites)
                                // If we were viewing an unfavorited page that just became favorite, exit override
                                if (overridePage != null && newFavorites.contains(overridePage)) {
                                    overridePage = null
                                    // Scroll to that page
                                    val index = newFavorites.indexOf(overridePage)
                                    coroutineScope.launch { pagerState.scrollToPage(index) }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    HomeTabsScreen(
                        favorites = favorites,
                        pagerState = pagerState,
                        currentPage = currentPage,
                        overridePage = overridePage,
                        onCloseOverride = { overridePage = null },
                        onNavigateToPage = { label ->
                            // This could be used if we want extra navigation logic, but we already handle in Footer's onItemSelected
                        },
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            startStepService()
        }
    }

    @SuppressLint("ServiceCast")
    private fun startStepService() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Log.d("MainActivity", "No step sensor, service not started")
            return
        }
        val intent = Intent(this, StepService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

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