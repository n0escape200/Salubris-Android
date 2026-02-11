package com.example.salubris.ui.screens.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.salubris.ui.components.PageModal
import com.example.salubris.ui.screens.subpages.Macros
import com.example.salubris.ui.screens.subpages.Steps
import com.example.salubris.ui.screens.subpages.Water


val tabs = mapOf<String, @Composable () -> Unit>(
    "Macros" to { Macros() },
    "Water" to { Water() },
    "Steps" to { Steps() }
)


@Composable
fun Tracking(navController: NavController) {
    Column() {
        PageModal(tabs)
    }
}