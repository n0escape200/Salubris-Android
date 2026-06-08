package com.example.salubris.ui.screens.subpages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.entities.MealWithProducts
import com.example.salubris.database.entities.Product
import com.example.salubris.database.viewmodels.*
import com.example.salubris.ui.components.FilterableDropdown
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.theme.*
import com.example.salubris.utils.ProductNutritionLabel
import com.example.salubris.utils.calculateMacrosForProduct
import com.example.salubris.utils.truncate2Decimals
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Macros(
    productViewModel: ProductViewModel = viewModel(factory = productViewModelFactory(LocalContext.current)),
    mealViewModel: MealViewModel = viewModel(factory = mealViewModelFactory(LocalContext.current)),
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
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } ?: ""
    }

    var openProducts by remember { mutableStateOf(false) }
    var openMeals by remember { mutableStateOf(false) }

    var trackedItems by remember { mutableStateOf<List<TrackedItem>>(emptyList()) }
    var totalMacros by remember {
        mutableStateOf(
            mapOf(
                "calories" to 0f,
                "protein" to 0f,
                "carbs" to 0f,
                "fats" to 0f
            )
        )
    }
    var reload by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    val settings by settingViewModel.settings.collectAsStateWithLifecycle()
    val settingsMap = remember(settings) { settings.associate { it.name to it.value } }
    val goalCalories = settingsMap["recommended_calories"]?.toIntOrNull() ?: 0
    val goalType = settingsMap["user_goal"] ?: "MAINTAIN"

    LaunchedEffect(datePickerState.selectedDateMillis, reload) {
        val selectedDate = datePickerState.selectedDateMillis
        if (selectedDate != null) {
            val zone = ZoneId.systemDefault()
            val startOfDay = Instant.ofEpochMilli(selectedDate)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()

            val items = macroViewModel.getTrackedItemsForDay(startOfDay)
            trackedItems = items

            val macros = mutableMapOf(
                "calories" to 0f,
                "protein" to 0f,
                "carbs" to 0f,
                "fats" to 0f
            )
            items.forEach { item ->
                macros["calories"] = macros["calories"]!! + item.calories
                macros["protein"] = macros["protein"]!! + item.protein
                macros["carbs"] = macros["carbs"]!! + item.carbs
                macros["fats"] = macros["fats"]!! + item.fats
            }
            totalMacros = macros
        }
        reload = false
    }

    Box {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { openMeals = true },
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
                    onClick = { openProducts = true },
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
                    Icon(Icons.AutoMirrored.Filled.Assignment, null, tint = Color.White, modifier = Modifier.size(30.dp))
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
                    MacroBadge("Kcal", totalMacros["calories"]!!.truncate2Decimals().toString(), caloriesColor)
                    MacroBadge("Protein", totalMacros["protein"]!!.truncate2Decimals().toString(), proteinColor)
                    MacroBadge("Carbs", totalMacros["carbs"]!!.truncate2Decimals().toString(), carbsColor)
                    MacroBadge("Fats", totalMacros["fats"]!!.truncate2Decimals().toString(), fatsColor)
                }
            }

            Column(
                modifier = Modifier
                    .background(ContainerBackground, RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trackedItems) { item ->
                        Row(
                            modifier = Modifier
                                .background(Color(60, 60, 60), shape = RoundedCornerShape(10.dp))
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(item.name, fontSize = 20.sp, fontWeight = FontWeight.W600, fontStyle = FontStyle.Italic, color = Color.White)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(item.calories.truncate2Decimals().toString(), color = caloriesColor)
                                        Text(item.protein.truncate2Decimals().toString(), color = proteinColor)
                                        Text(item.carbs.truncate2Decimals().toString(), color = carbsColor)
                                        Text(item.fats.truncate2Decimals().toString(), color = fatsColor)
                                    }
                                    Text(
                                        text = if (item.type == "product") "Amount: ${item.amountOrMultiplier}g" else "Quantity: ${item.amountOrMultiplier}g",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (item.type == "product") {
                                            macroViewModel.deleteMacroById(item.id)
                                        } else {
                                            macroViewModel.deleteTrackedMealById(item.id)
                                        }
                                        reload = true
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        cancelColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(Icons.Default.Delete, null, tint = cancelColor, modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                }
            }
        }

        // Product selection dialog
        if (openProducts) {
            Dialog(
                onDismissRequest = { openProducts = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { openProducts = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight()
                            .clickable { } // prevent dismiss when tapping inside
                            .background(Color(30, 30, 30), shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(30, 30, 30)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Add a product", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { openProducts = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                            ProductSelectionContent(
                                onAdd = { product, amount ->
                                    macroViewModel.saveMacroLine(product.name, product.calories, product.protein, product.carbs, product.fats, amount, System.currentTimeMillis())
                                    reload = true
                                    openProducts = false
                                },
                                productViewModel = productViewModel,
                                settingViewModel = settingViewModel,
                                currentCalories = totalMacros["calories"]!!,
                                goalCalories = goalCalories,
                                goalType = goalType
                            )
                        }
                    }
                }
            }
        }

        // Meal selection dialog
        if (openMeals) {
            Dialog(
                onDismissRequest = { openMeals = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { openMeals = false },
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Add a meal", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { openMeals = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                            MealSelectionContent(
                                meals = mealViewModel.mealsWithProducts.collectAsState().value,
                                onAdd = { mealId, quantityGrams ->
                                    macroViewModel.saveMeal(mealId, quantityGrams, System.currentTimeMillis())
                                    reload = true
                                    openMeals = false
                                },
                                currentCalories = totalMacros["calories"]!!,
                                goalCalories = goalCalories,
                                goalType = goalType
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
}

@Composable
private fun GoalFeedbackPreview(
    currentCalories: Float,
    addedCalories: Float,
    goalCalories: Int,
    goalType: String
) {
    val newTotal = currentCalories + addedCalories
    val remaining = goalCalories - newTotal
    val percentage = (newTotal / goalCalories).coerceIn(0f, 1f)
    val isLoss = goalType.contains("LOSS")
    val isGain = goalType.contains("GAIN")
    val isMaintain = goalType == "MAINTAIN"

    val message = when {
        isLoss -> {
            if (remaining > 0) "After adding: 🎯 $remaining kcal left to stay in deficit"
            else "⚠️ After adding: You will exceed your calorie goal"
        }
        isGain -> {
            if (remaining > 0) "After adding: 💪 $remaining more kcal needed to reach surplus"
            else "✅ After adding: You will meet or exceed your surplus goal"
        }
        isMaintain -> {
            if (remaining > 0) "$remaining kcal remaining to maintain weight"
            else if (remaining < 0) "Will exceed maintenance by ${-remaining.toInt()} kcal"
            else "Perfect! You'll exactly hit your maintenance goal"
        }
        else -> ""
    }

    val barColor = when {
        remaining >= 0 -> when {
            isLoss -> submitColor
            isGain -> Color(0xFF4CAF50)
            else -> Color.LightGray
        }
        else -> cancelColor
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ContainerBackground, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text("Effect on your daily goal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Color.Gray.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(4.dp))
        Text(message, color = Color.White, fontSize = 12.sp)
        Text(
            text = "${newTotal.toInt()} / $goalCalories kcal",
            color = Color.LightGray,
            fontSize = 11.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSelectionContent(
    onAdd: (Product, Float) -> Unit,
    productViewModel: ProductViewModel,
    settingViewModel: SettingViewModel,
    currentCalories: Float,
    goalCalories: Int,
    goalType: String
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var amount by remember { mutableStateOf("") }
    var calories by remember { mutableFloatStateOf(0f) }
    var protein by remember { mutableFloatStateOf(0f) }
    var carbs by remember { mutableFloatStateOf(0f) }
    var fats by remember { mutableFloatStateOf(0f) }

    val options by productViewModel.products.collectAsState()
    val mainGoal = settingViewModel.getSettingByName("goal_main")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FilterableDropdown(
            options = options,
            selectedItem = selectedProduct,
            onItemSelected = { product ->
                selectedProduct = product
            },
            label = "Select a product",
            displayText = { it.name }
        )

        if (selectedProduct != null) {
            ProductNutritionLabel(selectedProduct!!)
            Input(
                label = "Amount (g)",
                value = amount,
                onChange = { value ->
                    amount = value
                    val safeAmount = (value.toFloatOrNull() ?: 0f) / 100
                    calories = selectedProduct!!.calories * safeAmount
                    protein = selectedProduct!!.protein * safeAmount
                    carbs = selectedProduct!!.carbs * safeAmount
                    fats = selectedProduct!!.fats * safeAmount
                },
                keyboardType = KeyboardType.Number
            )

            if (goalCalories > 0 && amount.isNotEmpty() && amount.toFloatOrNull() != null) {
                Spacer(modifier = Modifier.height(8.dp))
                GoalFeedbackPreview(
                    currentCalories = currentCalories,
                    addedCalories = calories,
                    goalCalories = goalCalories,
                    goalType = goalType
                )
            } else if (goalCalories == 0) {
                Text("No goal set", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.W600)
                Text(
                    "We recommend that you set a main goal inside the settings section for a better experience",
                    color = Color(204, 204, 204, 255),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W600,
                    fontStyle = FontStyle.Italic
                )
            }

            Column(
                modifier = Modifier
                    .background(Color(73, 73, 73, 255), RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Preview macro intake", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.W600)
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
        } else {
            Text(
                "Please select a product to continue",
                color = Color(154, 154, 154, 255),
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic
            )
        }

        Button(
            onClick = {
                if (selectedProduct != null && amount.isNotEmpty()) {
                    onAdd(selectedProduct!!, amount.toFloat())
                }
            },
            enabled = selectedProduct != null && amount.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = productColor)
        ) {
            Text("Add to today", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealSelectionContent(
    meals: List<MealWithProducts>,
    onAdd: (Int, Float) -> Unit,
    currentCalories: Float,
    goalCalories: Int,
    goalType: String
) {
    var selectedMeal by remember { mutableStateOf<MealWithProducts?>(null) }
    var quantity by remember { mutableStateOf("") }

    var previewCalories by remember { mutableFloatStateOf(0f) }
    var previewProtein by remember { mutableFloatStateOf(0f) }
    var previewCarbs by remember { mutableFloatStateOf(0f) }
    var previewFats by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(selectedMeal, quantity) {
        if (selectedMeal != null && quantity.toFloatOrNull() != null) {
            val quantityGrams = quantity.toFloat()
            val totalWeight = selectedMeal!!.products.sumOf { it.quantity.toDouble() }.toFloat()
            val multiplier = if (totalWeight > 0) quantityGrams / totalWeight else 0f

            var totalCal = 0f
            var totalProt = 0f
            var totalCarb = 0f
            var totalFat = 0f

            selectedMeal!!.products.forEach { productWithQty ->
                val amount = productWithQty.quantity * multiplier
                val macros = calculateMacrosForProduct(productWithQty.product, amount)
                totalCal += macros["calories"] ?: 0f
                totalProt += macros["protein"] ?: 0f
                totalCarb += macros["carbs"] ?: 0f
                totalFat += macros["fats"] ?: 0f
            }
            previewCalories = totalCal
            previewProtein = totalProt
            previewCarbs = totalCarb
            previewFats = totalFat
        } else {
            previewCalories = 0f
            previewProtein = 0f
            previewCarbs = 0f
            previewFats = 0f
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FilterableDropdown(
            options = meals,
            selectedItem = selectedMeal,
            onItemSelected = { meal ->
                selectedMeal = meal
            },
            label = "Select a meal",
            displayText = { it.meal.name },
            modifier = Modifier.fillMaxWidth()
        )

        if (selectedMeal != null) {
            Text("Meal contains:", color = Color.White, fontWeight = FontWeight.Bold)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                selectedMeal!!.products.forEach { productWithQty ->
                    Text(
                        "• ${productWithQty.product.name} (${productWithQty.quantity}g)",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val totalWeight = selectedMeal!!.products.sumOf { it.quantity.toDouble() }.toFloat()
            Text("Total meal weight: ${totalWeight.truncate2Decimals()}g", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Input(
                label = "Quantity consumed (g)",
                value = quantity,
                onChange = { quantity = it },
                keyboardType = KeyboardType.Number
            )

            val quantityFloat = quantity.toFloatOrNull()
            if (quantityFloat != null && quantityFloat > 0) {
                val multiplier = quantityFloat / totalWeight
                Text(
                    "Serving factor: ${multiplier.truncate2Decimals()}x",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            if (goalCalories > 0 && quantity.isNotEmpty() && quantityFloat != null && quantityFloat > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                GoalFeedbackPreview(
                    currentCalories = currentCalories,
                    addedCalories = previewCalories,
                    goalCalories = goalCalories,
                    goalType = goalType
                )
            } else if (goalCalories == 0) {
                Text("No goal set", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.W600)
                Text(
                    "We recommend that you set a main goal inside the settings section for a better experience",
                    color = Color(204, 204, 204, 255),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W600,
                    fontStyle = FontStyle.Italic
                )
            }

            if (quantityFloat != null && quantityFloat > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(73, 73, 73, 255), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Macro preview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MacroBadge("Kcal", previewCalories.truncate2Decimals().toString(), caloriesColor)
                        MacroBadge("Protein", previewProtein.truncate2Decimals().toString(), proteinColor)
                        MacroBadge("Carbs", previewCarbs.truncate2Decimals().toString(), carbsColor)
                        MacroBadge("Fats", previewFats.truncate2Decimals().toString(), fatsColor)
                    }
                }
            }
        } else {
            Text(
                "Please select a meal",
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }

        Button(
            onClick = {
                if (selectedMeal != null && quantity.toFloatOrNull() != null) {
                    onAdd(selectedMeal!!.meal.uid, quantity.toFloat())
                }
            },
            enabled = selectedMeal != null && quantity.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = productColor)
        ) {
            Text("Add to today", color = Color.White)
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