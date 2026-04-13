package com.example.salubris

import HomeTabsScreen
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.example.salubris.database.AppDatabase
import com.example.salubris.stepcounter.StepService
import com.example.salubris.ui.components.Footer
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Launcher for multiple permissions
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
    }
}