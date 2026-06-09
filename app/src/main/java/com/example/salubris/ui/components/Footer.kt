package com.example.salubris.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.salubris.ui.theme.*

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun Footer(
    currentPage: String,
    favorites: List<String>,
    allPages: List<String> = listOf("Home", "Tracking", "Products", "Meals", "Settings"),
    onItemSelected: (String) -> Unit,
    onUpdateFavorites: (List<String>) -> Unit,
    actions: List<FooterAction> = emptyList()
) {
    val configuration = LocalConfiguration.current
    val showLabels = configuration.screenWidthDp >= 360

    var showModal by remember { mutableStateOf(false) }
    var tempFavorites by remember { mutableStateOf(favorites) }

    LaunchedEffect(showModal) {
        if (showModal) {
            tempFavorites = favorites.toList()
        }
    }

    fun togglePage(page: String, add: Boolean) {
        val newList = if (add) {
            if (tempFavorites.size < 4) tempFavorites + page else tempFavorites
        } else {
            if (tempFavorites.size > 1) tempFavorites - page else tempFavorites
        }
        if (newList != tempFavorites) {
            val sortedList = newList.sortedBy { allPages.indexOf(it) }
            tempFavorites = sortedList
            onUpdateFavorites(sortedList)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Footer bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(ContainerBackground, shape = MainContainerBorder)
                .padding(vertical = 7.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { index ->
                if (index < favorites.size) {
                    FooterNavItem(
                        label = favorites[index],
                        icon = getIconForPage(favorites[index]),
                        isSelected = currentPage == favorites[index],
                        showLabel = showLabels,
                        onClick = { onItemSelected(favorites[index]) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.width(80.dp))
            for (index in 2 until 4) {
                if (index < favorites.size) {
                    FooterNavItem(
                        label = favorites[index],
                        icon = getIconForPage(favorites[index]),
                        isSelected = currentPage == favorites[index],
                        showLabel = showLabels,
                        onClick = { onItemSelected(favorites[index]) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Cut‑out circle background
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
                .size(90.dp)
                .clip(CircleShape)
                .background(ContainerBackground)
        )

        // FAB button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 8.dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(productColor)
                .clickable { showModal = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Menu, "Menu", tint = Color.White, modifier = Modifier.size(36.dp))
        }
    }

    // Modal for page selection and extra actions
    if (showModal) {
        Dialog(
            onDismissRequest = { showModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showModal = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight()
                        .clickable { }
                        .background(Color(30, 30, 30), shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(30, 30, 30)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Customize Footer Pages",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // Page selection
                        allPages.forEach { page ->
                            val isChecked = tempFavorites.contains(page)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(Color.Transparent),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox area (only toggles favorite)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { togglePage(page, !isChecked) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                // Navigation area (click to navigate to page)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            onItemSelected(page)
                                            showModal = false
                                        }
                                        .padding(end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = getIconForPage(page),
                                        contentDescription = page,
                                        tint = if (currentPage == page) productColor else Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = page,
                                        fontSize = 18.sp,
                                        color = if (currentPage == page) productColor else Color.White,
                                        fontWeight = if (currentPage == page) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                            if (page != allPages.last()) {
                                Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 0.5.dp)
                            }
                        }
                        // Extra actions
                        if (actions.isNotEmpty()) {
                            Divider()
                            actions.forEach { action ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .clickable {
                                            action.onClick()
                                            showModal = false
                                        }
                                        .background(Color.Transparent),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            action.icon,
                                            contentDescription = action.contentDescription,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Text(
                                        action.contentDescription,
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }
                            }
                        }
                        if (tempFavorites.isEmpty()) {
                            Text(
                                "Select at least 1 page",
                                color = Color.Yellow,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showModal = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = productColor)
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FooterNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    showLabel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) productColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(20.dp))
            if (showLabel) {
                Text(label, color = Color.White, fontSize = 10.sp)
            }
        }
    }
}

private fun getIconForPage(page: String): ImageVector {
    return when (page) {
        "Home" -> Icons.Default.Home
        "Tracking" -> Icons.Default.Create
        "Products" -> Icons.Default.Kitchen
        "Meals" -> Icons.AutoMirrored.Filled.MenuBook
        "Settings" -> Icons.Default.Settings
        else -> Icons.Default.Home
    }
}

data class FooterAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)