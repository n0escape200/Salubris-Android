package com.example.salubris.ui.screens.subpages

import Modal
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.entities.Product
import com.example.salubris.database.relations.MacroWithProduct
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.theme.*
import com.example.salubris.utils.truncate2Decimals
import com.example.salubris.viewmodels.MacroViewModel
import com.example.salubris.viewmodels.ProductViewModel
import com.example.salubris.viewmodels.SettingViewModel
import com.example.salubris.viewmodels.macroViewModelFactory
import com.example.salubris.viewmodels.productViewModelFactory
import com.example.salubris.viewmodels.settingsViewModelFactory
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Macros(
    productViewModel: ProductViewModel = viewModel(factory = productViewModelFactory(LocalContext.current)),
    settingViewModel: SettingViewModel = viewModel(factory = settingsViewModelFactory(LocalContext.current)),
    macroViewModel: MacroViewModel = viewModel(factory = macroViewModelFactory(LocalContext.current))
) {
    val todayMillis = Instant.now()
        .atZone(ZoneId.of("UTC"))
        .toLocalDate()
        .atStartOfDay(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = todayMillis)
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedDateText = remember(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())  // convert to local timezone
                .toLocalDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } ?: ""
    }

    var openProducts by remember { mutableStateOf(false) }
    var trackLines by remember { mutableStateOf<List<MacroWithProduct>>(emptyList()) }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        val selectedDate = datePickerState.selectedDateMillis
        if (selectedDate != null) {
            val zone = ZoneId.systemDefault()

            val startOfDay = Instant.ofEpochMilli(selectedDate)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()

            trackLines = macroViewModel.getMacrosPerDay(startOfDay)
            Log.v("TAG", "$trackLines")
        }
    }
    Box {
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
                    Icon(
                        Icons.Default.Flatware,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Meals", color = Color.White)
                }
                Text("/", color = Color.White, fontWeight = FontWeight.W800, fontSize = 30.sp)
                Button(
                    onClick = { openProducts = true },
                    colors = ButtonDefaults.buttonColors(mealColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Fastfood,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
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
                            .clickable { showDatePicker = true })
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    
                }
            }
        }
        val options by productViewModel.products.collectAsState()
        var expanded by remember { mutableStateOf(false) }
        var selectedProduct by remember { mutableStateOf<Product?>(null) }
        var amount by remember { mutableStateOf("") }
        val mainGoal = settingViewModel.getSettingByName("goal_main")

        var calories by remember { mutableFloatStateOf(0f) }
        var protein by remember { mutableFloatStateOf(0f) }
        var carbs by remember { mutableFloatStateOf(0f) }
        var fats by remember { mutableFloatStateOf(0f) }

        Modal(
            open = openProducts,
            onClose = { openProducts = false },
            onSubmit = {
                if(selectedProduct != null && amount != ""){
                    macroViewModel.saveMacroLine(selectedProduct!!.uid,amount.toFloat(),System.currentTimeMillis())
                    openProducts = false
                }
            },
            title = "Add a product"
        ) {

            ExposedDropdownMenuBox(
                expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select a product") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        if (selectedProduct != null) {
                            IconButton(onClick = { selectedProduct = null }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear selection",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
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
                        .border(0.5.dp, Color.White)
                ) {
                    if (options.isNotEmpty()) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        selectionOption.name, color = Color.White
                                    )
                                }, onClick = {
                                    selectedProduct = selectionOption
                                    expanded = false
                                }, modifier = Modifier.background(ContainerBackground)
                            )
                        }
                    } else {
                        DropdownMenuItem(
                            text = { Text("No options", color = Color(157, 157, 157, 255)) },
                            onClick = { expanded = false },
                            modifier = Modifier.background(ContainerBackground)
                        )
                    }
                }
            }

            if (selectedProduct != null) {
                ProductNutritionLabel(selectedProduct!!)
                Input("Amount", amount, onChange = { value ->
                    amount = value
                    val safeAmount = (value.toFloatOrNull() ?: 0f) / 100
                    calories = selectedProduct!!.calories * safeAmount
                    protein = selectedProduct!!.protein * safeAmount
                    carbs = selectedProduct!!.carbs * safeAmount
                    fats = selectedProduct!!.fats * safeAmount
                }, keyboardType = KeyboardType.Number)

                if (mainGoal == null) {
                    Text(
                        "No goal set",
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.W600
                    )
                    Text(
                        "We recommend that you set a main goal inside the settings section for a better experience",
                        color = Color(204, 204, 204, 255),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W600,
                        fontStyle = FontStyle.Italic
                    )

                    Column(
                        modifier = Modifier
                            .background(
                                Color(73, 73, 73, 255), RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Preview macro intake",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W600
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MacroBadge("Kcal", calories.truncate2Decimals().toString(), caloriesColor)
                            MacroBadge("Protein", protein.truncate2Decimals().toString(), proteinColor)
                            MacroBadge("Carbs", carbs.truncate2Decimals().toString(), carbsColor)
                            MacroBadge("Fats", fats.truncate2Decimals().toString(), fatsColor)
                        }
                    }
                }
            } else {
                Text(
                    "Please select a product to continue",
                    color = Color(154, 154, 154, 255),
                    fontSize = 17.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = { showDatePicker = false }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
        }) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
}

@Composable
private fun MacroBadge(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .background(color, RoundedCornerShape(5.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = Color.White)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProductNutritionLabel(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(45, 45, 45), RoundedCornerShape(10.dp))
            .border(1.dp, Color.White, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Nutrition per 100g",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Calories", color = Color.White)
            Text("${product.calories}", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Protein", color = Color.White)
            Text("${product.protein} g", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Carbs", color = Color.White)
            Text("${product.carbs} g", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Fats", color = Color.White)
            Text("${product.fats} g", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}