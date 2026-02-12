package com.example.salubris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.ui.Alignment
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.MainContainerBorder

@Composable
fun Footer(currentPage: String, onItemSelected: (String) -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 15.dp)
            .background(ContainerBackground, shape = MainContainerBorder),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // List of items from FooterItems
        val items = listOf(
            FooterItemData("Home", Icons.Default.Home),
            FooterItemData("Tracking", Icons.Default.Create),
            FooterItemData("Products", Icons.Default.Kitchen)
        )

        items.forEach { item ->
            val isSelected = currentPage == item.label

            Box(
                modifier = Modifier
                    .padding(0.dp,2.dp,0.dp,2.dp)
                    .background(
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onItemSelected(item.label) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) Color.Black else Color.White
                    )
                    Text(
                        text = item.label,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

// Helper data class for footer items
data class FooterItemData(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
