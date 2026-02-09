package com.example.salubris

import HomeTabsScreen
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.salubris.ui.navigation.Routes

@Composable
fun AppNav(
    modifier: Modifier = Modifier,
    currentPage: MutableState<String>? = null,
    pagerState: PagerState
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        modifier = modifier
    ) {
        composable(Routes.Home) {
            HomeTabsScreen(
                navController = navController,
                pagerState = pagerState,
                currentPage = currentPage,
                modifier = modifier
            )
        }
    }
}
