package com.example.salubris.ui.screens.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.salubris.ui.components.PageModal
import com.example.salubris.ui.screens.subpages.Macros


val tabs = mapOf<String, @Composable () -> Unit>(
    "Macros" to { Macros() },
)


@Composable
fun Tracking(navController: NavController) {
    Column(){
        PageModal(tabs)
    }
}