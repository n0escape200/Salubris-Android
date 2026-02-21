package com.example.salubris.ui.screens.subpages

import Modal
import android.R
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.ui.theme.carbsColor
import com.example.salubris.ui.theme.fatsColor
import com.example.salubris.ui.theme.mealColor
import com.example.salubris.ui.theme.productColor
import com.example.salubris.ui.theme.proteinColor
import com.example.salubris.viewmodels.ProductViewModel
import com.example.salubris.viewmodels.SettingViewModel
import com.example.salubris.viewmodels.productViewModelFactory
import com.example.salubris.viewmodels.settingsViewModelFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Macros(
    productViewModel: ProductViewModel = viewModel(
        factory = productViewModelFactory(LocalContext.current)
    ),
    settingViewModel: SettingViewModel = viewModel(
        factory = settingsViewModelFactory(LocalContext.current)
    ),
) {

    val todayMillis = remember {
        LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
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
    var openMeals by remember { mutableStateOf(false) }
    var openProducts by remember { mutableStateOf(false) }


    Box(){
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
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
                Text("/", color = Color.White, fontWeight = FontWeight.W800, fontSize = 30.sp)
                Button(
                    onClick = {openProducts = true},
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
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),

                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
//                repeat(30){
//                    MacroLine()
//                }
            }
        }
        Modal(
            open = openProducts,
            onClose = {openProducts = false},
            onSubmit = {},
            title = "Add a product"
        ) {
            val options = listOf("Option 1", "Option 2", "Option 3")
            var expanded by remember { mutableStateOf(false) }
            var selectedText by remember { mutableStateOf(options[0]) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Option") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier.menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(ContainerBackground)
                ) {
                    options.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(text = selectionOption, color = Color.White) },
                            onClick = {
                                selectedText = selectionOption
                                expanded = false
                            },
                            modifier = Modifier
                                .background(ContainerBackground)
                        )
                    }
                }
            }
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
