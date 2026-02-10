package com.example.salubris

import HomeTabsScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.salubris.ui.components.Footer
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val pagerState = rememberPagerState(pageCount = { 2 })
            val currentPage = remember { mutableStateOf("Home") }
            val coroutineScope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1E1E), // top color
                                Color(0xFF121212)  // bottom color
                            )
                        )
                    )
            ) {
                Scaffold(
                    containerColor = Color.Transparent, bottomBar = {
                        Footer(
                            currentPage = currentPage.value, onItemSelected = { label ->
                                val page = when (label) {
                                    "Home" -> 0
                                    "Tracking" -> 1
                                    else -> 0
                                }
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            })
                    }) { innerPadding ->
                    HomeTabsScreen(
                        navController = rememberNavController(),
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
}
