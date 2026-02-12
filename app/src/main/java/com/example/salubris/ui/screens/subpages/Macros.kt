package com.example.salubris.ui.screens.subpages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.salubris.ui.components.MacroLine
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.ui.theme.carbsColor
import com.example.salubris.ui.theme.fatsColor
import com.example.salubris.ui.theme.mealColor
import com.example.salubris.ui.theme.productColor
import com.example.salubris.ui.theme.proteinColor
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Macros() {

    val todayMillis = remember {
        java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayMillis
    )

    var showDatePicker by remember { mutableStateOf(false) }

    val selectedDateText = remember(datePickerState.selectedDateMillis) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(datePickerState.selectedDateMillis!!))
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(productColor),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Flatware, null, tint = Color.White, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(6.dp))
                Text("Meals", color = Color.White)
            }

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(mealColor),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Fastfood, null, tint = Color.White, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(6.dp))
                Text("Products", color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ContainerBackground, RoundedCornerShape(10.dp))
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(Modifier.width(10.dp))

                Text("Macros for ", color = Color.White, fontWeight = FontWeight.W600)

                Text(
                    text = selectedDateText,
                    color = Color.White,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .background(Color.DarkGray, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable { showDatePicker = true }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroBadge("Kcal", "2500", caloriesColor)
                MacroBadge("Protein", "2500", proteinColor)
                MacroBadge("Carbs", "2500", carbsColor)
                MacroBadge("Fats", "2500", fatsColor)
            }
        }

        Column(
            modifier = Modifier
                .background(ContainerBackground, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MacroLine()
            MacroLine()
            MacroLine()
            MacroLine()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
private fun MacroBadge(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .background(color, RoundedCornerShape(5.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, color = Color.White)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
