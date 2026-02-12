package com.example.salubris.ui.screens.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.example.salubris.ui.components.PageModal
import com.example.salubris.ui.screens.subpages.Macros
import com.example.salubris.ui.screens.subpages.Steps
import com.example.salubris.ui.screens.subpages.Water


@RequiresApi(Build.VERSION_CODES.O)
val tabs = mapOf<String, @Composable () -> Unit>(
    "Macros" to { Macros() },
    "Water" to { Water() },
    "Steps" to { Steps() },
)


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Tracking() {
    Column() {
        PageModal(tabs)
    }
}