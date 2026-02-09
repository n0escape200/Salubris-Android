package com.example.salubris

import HomeTabsScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

            Scaffold(
                containerColor = Color.Black,
                bottomBar = {
                    Footer(
                        currentPage = currentPage.value,
                        onItemSelected = { label ->
                            val page = when (label) {
                                "Home" -> 0
                                "Tracking" -> 1
                                else -> 0
                            }
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                HomeTabsScreen(
                    navController = rememberNavController(),
                    pagerState = pagerState,
                    currentPage = currentPage,
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 10.dp)
                )
            }
        }
    }
}
