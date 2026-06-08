package com.example.salubris

import HomeTabsScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.salubris.ui.navigation.Routes

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNav(
    favorites: List<String>,
    pagerState: PagerState,
    currentPage: MutableState<String>,
    overridePage: String?,
    onCloseOverride: () -> Unit,
    onNavigateToPage: (String) -> Unit,  // added
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        modifier = modifier
    ) {
        composable(Routes.Home) {
            HomeTabsScreen(
                favorites = favorites,
                pagerState = pagerState,
                currentPage = currentPage,
                overridePage = overridePage,
                onCloseOverride = onCloseOverride,
                onNavigateToPage = onNavigateToPage,
                modifier = modifier
            )
        }
    }
}