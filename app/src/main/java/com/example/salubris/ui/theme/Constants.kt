package com.example.salubris.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.unit.dp

val MainContainerBorder = RoundedCornerShape(10.dp)

object FooterItems{
    val Home = {
        val label = "Home"
        val icon = Icons.Default.Home
    }
    val Tracking = {
        val label = "Tracking"
        val icon = Icons.Default.Create
    }
}