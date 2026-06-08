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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.MainContainerBorder
import com.example.salubris.ui.theme.productColor

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun Footer(
    currentPage: String,
    favorites: List<String>,
    allPages: List<String> = listOf("Home", "Tracking", "Products", "Meals", "Settings"),
    onItemSelected: (String) -> Unit,
    onUpdateFavorites: (List<String>) -> Unit
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
        modifier = Modifier.fillMaxWidth().padding(10.dp),
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
            // Left two items – each takes equal weight
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
            // Spacer for the FAB area – increased to avoid overlap
            Spacer(modifier = Modifier.width(80.dp))
            // Right two items
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

        // FAB
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
            Icon(Icons.Default.Menu, "Menu", tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }

    // Full‑screen modal (unchanged except bottom padding adjustment)
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
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight()
                        .padding(bottom = 140.dp)
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
                        allPages.forEach { page ->
                            val isChecked = tempFavorites.contains(page)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(Color.Transparent),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                        if (tempFavorites.isEmpty()) {
                            Text(
                                text = "Select at least 1 page",
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
            Icon(icon, label, tint = if (isSelected) Color.Black else Color.White, modifier = Modifier.size(20.dp))
            if (showLabel) {
                Text(label, color = if (isSelected) ContainerBackground else Color.White, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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