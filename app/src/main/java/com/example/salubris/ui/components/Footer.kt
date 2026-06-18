package com.example.salubris.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.MainContainerBorder
import com.example.salubris.ui.theme.cancelColor
import com.example.salubris.ui.theme.productColor
import com.example.salubris.utils.Vocabulary

// -------- Star toggle composable --------
@Composable
private fun StarToggle(checked: Boolean, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Icon(
        imageVector = if (checked) Icons.Default.Star else Icons.Default.StarBorder,
        contentDescription = if (checked) Vocabulary.get().selected else Vocabulary.get().notSelected,
        tint = when {
            !enabled -> Color.Gray
            checked -> productColor
            else -> Color.LightGray
        },
        modifier = Modifier
            .size(28.dp)
            .clickable(enabled = enabled) { onToggle(!checked) }
    )
}

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
        if (add && tempFavorites.size >= 4) return
        val newList = if (add) {
            tempFavorites + page
        } else {
            tempFavorites - page
        }
        if (newList != tempFavorites) {
            val sortedList = newList.sortedBy { allPages.indexOf(it) }
            tempFavorites = sortedList
            onUpdateFavorites(sortedList)
        }
    }

    fun getDisplayName(page: String): String {
        return when (page) {
            "Home" -> Vocabulary.get().home
            "Tracking" -> Vocabulary.get().tracking
            "Products" -> Vocabulary.get().productsNav
            "Meals" -> Vocabulary.get().mealsNav
            "Settings" -> Vocabulary.get().settingsNav
            else -> page
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
                        label = getDisplayName(favorites[index]),
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
                        label = getDisplayName(favorites[index]),
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

        // Centre circular cutout (background for FAB)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
                .size(90.dp)
                .clip(CircleShape)
                .background(ContainerBackground)
        )

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 2.dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(productColor)
                .clickable { showModal = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Menu,
                Vocabulary.get().menu,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }

    // Compact popup menu anchored near the FAB
    if (showModal) {
        Dialog(
            onDismissRequest = { showModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showModal = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .wrapContentHeight()
                        .padding(bottom = 80.dp)
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(30, 30, 30)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            Vocabulary.get().customizeFooter,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // --- Pages section with star toggles ---
                        Text(
                            Vocabulary.get().pages,
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )

                        if (tempFavorites.size >= 4) {
                            Text(
                                Vocabulary.get().maximumPagesSelected,
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        allPages.forEach { page ->
                            val isChecked = tempFavorites.contains(page)
                            val canToggle = isChecked || tempFavorites.size < 4

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .clickable {
                                        onItemSelected(page)
                                        showModal = false
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StarToggle(checked = isChecked, enabled = canToggle) {
                                    togglePage(page, it)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    getIconForPage(page),
                                    contentDescription = getDisplayName(page),
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(getDisplayName(page), color = Color.White, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        // --- Quick Actions section ---
                        Text(
                            Vocabulary.get().quickActions,
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        // AI Chat action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable {
                                    showModal = false
                                    onOpenChat()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = Vocabulary.get().aiAssistant,
                                tint = productColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                Vocabulary.get().aiAssistant,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Close action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { showModal = false },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                Icons.Default.Close,
                                contentDescription = Vocabulary.get().close,
                                tint = cancelColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(Vocabulary.get().close, color = Color.White, fontSize = 16.sp)
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