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
    onOpenChat: () -> Unit,
    actions: List<FooterAction> = emptyList()
) {
    val configuration = LocalConfiguration.current
    val showLabels = configuration.screenWidthDp >= 360

    var showModal by remember { mutableStateOf(false) }
    var tempFavorites by remember { mutableStateOf(favorites) }

    LaunchedEffect(showModal) {
        if (showModal) tempFavorites = favorites.toList()
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

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)   // was -6.dp
                .size(90.dp)
                .clip(CircleShape)
                .background(ContainerBackground)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 2.dp)       // was 8.dp
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(productColor)
                .clickable { showModal = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Menu,
                "Menu",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }

    // Customisation dialog (near full‑screen)
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
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.9f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(30, 30, 30))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Customize Footer",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Pages",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        allPages.forEach { page ->
                            val isChecked = tempFavorites.contains(page)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { togglePage(page, it) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    getIconForPage(page),
                                    page,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(page, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Quick Actions",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        // AI Chat button – opens full‑screen chat
                        Button(
                            onClick = {
                                showModal = false
                                onOpenChat()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = productColor)
                        ) {
                            Icon(Icons.Default.Chat, "Chat", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Assistant", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showModal = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = submitColor)
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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