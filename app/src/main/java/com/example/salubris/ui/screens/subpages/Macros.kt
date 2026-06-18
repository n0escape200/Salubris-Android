package com.example.salubris.ui.screens.subpages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.salubris.database.viewmodels.MacroViewModel
import com.example.salubris.database.viewmodels.MealViewModel
import com.example.salubris.database.viewmodels.ProductViewModel
import com.example.salubris.database.viewmodels.SettingViewModel
import com.example.salubris.database.viewmodels.TrackedItem
import com.example.salubris.database.viewmodels.macroViewModelFactory
import com.example.salubris.database.viewmodels.mealViewModelFactory
import com.example.salubris.database.viewmodels.productViewModelFactory
import com.example.salubris.database.viewmodels.settingsViewModelFactory
import com.example.salubris.ui.components.FilterableDropdown
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.ui.theme.cancelColor
import com.example.salubris.ui.theme.carbsColor
import com.example.salubris.ui.theme.fatsColor
import com.example.salubris.ui.theme.mealColor
import com.example.salubris.ui.theme.productColor
import com.example.salubris.ui.theme.proteinColor
import com.example.salubris.ui.theme.submitColor
import com.example.salubris.utils.ProductNutritionLabel
import com.example.salubris.utils.Vocabulary
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
                macros["calories"] =
                    macros["calories"]!! + (item.calories * item.amountOrMultiplier / 100)
                macros["protein"] =
                    macros["protein"]!! + item.protein * item.amountOrMultiplier / 100
                macros["carbs"] = macros["carbs"]!! + item.carbs * item.amountOrMultiplier / 100
                macros["fats"] = macros["fats"]!! + item.fats * item.amountOrMultiplier / 100
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
                    Icon(
                        Icons.Default.Flatware,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(Vocabulary.get().meals, color = Color.White)
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
                    Text(Vocabulary.get().products, color = Color.White)
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
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        Vocabulary.get().macrosFor,
                        color = Color.White,
                        fontWeight = FontWeight.W600
                    )
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
                    MacroBadge(
                        Vocabulary.get().kcalShort,
                        totalMacros["calories"]!!.truncate2Decimals().toString(),
                        caloriesColor
                    )
                    MacroBadge(
                        Vocabulary.get().proteinShort,
                        totalMacros["protein"]!!.truncate2Decimals().toString(),
                        proteinColor
                    )
                    MacroBadge(
                        Vocabulary.get().carbsShort,
                        totalMacros["carbs"]!!.truncate2Decimals().toString(),
                        carbsColor
                    )
                    MacroBadge(
                        Vocabulary.get().fatsShort,
                        totalMacros["fats"]!!.truncate2Decimals().toString(),
                        fatsColor
                    )
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
                                    Text(
                                        item.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.W600,
                                        fontStyle = FontStyle.Italic,
                                        color = Color.White
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            (item.calories * (item.amountOrMultiplier / 100)).truncate2Decimals()
                                                .toString(),
                                            color = caloriesColor
                                        )
                                        Text(
                                            (item.protein * (item.amountOrMultiplier / 100)).truncate2Decimals()
                                                .toString(),
                                            color = proteinColor
                                        )
                                        Text(
                                            (item.carbs * (item.amountOrMultiplier / 100)).truncate2Decimals()
                                                .toString(),
                                            color = carbsColor
                                        )
                                        Text(
                                            (item.fats * (item.amountOrMultiplier / 100)).truncate2Decimals()
                                                .toString(),
                                            color = fatsColor
                                        )
                                    }
                                    Text(
                                        text = if (item.type == "product") {
                                            String.format(
                                                Vocabulary.get().amountLabel,
                                                item.amountOrMultiplier
                                            )
                                        } else {
                                            String.format(
                                                Vocabulary.get().quantityLabelMeal,
                                                item.amountOrMultiplier
                                            )
                                        },
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
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = cancelColor,
                                    modifier = Modifier.size(30.dp)
                                )
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
                                Text(
                                    Vocabulary.get().addProductTitle,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { openProducts = false }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = Vocabulary.get().close,
                                        tint = Color.White
                                    )
                                }
                            }
                            ProductSelectionContent(
                                onAdd = { product, amount ->
                                    macroViewModel.saveMacroLine(
                                        product.name,
                                        product.calories,
                                        product.protein,
                                        product.carbs,
                                        product.fats,
                                        amount,
                                        System.currentTimeMillis()
                                    )
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
                                Text(
                                    Vocabulary.get().addMealTitle,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { openMeals = false }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = Vocabulary.get().close,
                                        tint = Color.White
                                    )
                                }
                            }
                            MealSelectionContent(
                                meals = mealViewModel.mealsWithProducts.collectAsState().value,
                                onAdd = { mealId, quantityGrams ->
                                    macroViewModel.saveMeal(
                                        mealId,
                                        quantityGrams,
                                        System.currentTimeMillis()
                                    )
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
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(Vocabulary.get().ok) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(Vocabulary.get().cancel) }
            }
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
            if (remaining > 0) String.format(Vocabulary.get().afterAddingDeficit, remaining.toInt())
            else Vocabulary.get().afterAddingExceeded
        }

        isGain -> {
            if (remaining > 0) String.format(Vocabulary.get().afterAddingSurplus, remaining.toInt())
            else Vocabulary.get().afterAddingGoalMet
        }

        isMaintain -> {
            if (remaining > 0) String.format(
                Vocabulary.get().remainingToMaintain,
                remaining.toInt()
            )
            else if (remaining < 0) String.format(
                Vocabulary.get().exceedMaintenance,
                (-remaining).toInt()
            )
            else Vocabulary.get().perfectMaintain
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
        Text(
            Vocabulary.get().effectOnGoal,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
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
            label = Vocabulary.get().selectProduct,
            displayText = { it.name }
        )

        if (selectedProduct != null) {
            ProductNutritionLabel(selectedProduct!!)
            Input(
                label = String.format(Vocabulary.get().amountGrams, "")
                    .trim(), // simpler: use existing key
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
                Text(
                    Vocabulary.get().noGoalSet,
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.W600
                )
                Text(
                    Vocabulary.get().recommendSetGoal,
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
                Text(
                    Vocabulary.get().previewMacroIntake,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MacroBadge(
                        Vocabulary.get().kcalShort,
                        calories.truncate2Decimals().toString(),
                        caloriesColor
                    )
                    MacroBadge(
                        Vocabulary.get().proteinShort,
                        protein.truncate2Decimals().toString(),
                        proteinColor
                    )
                    MacroBadge(
                        Vocabulary.get().carbsShort,
                        carbs.truncate2Decimals().toString(),
                        carbsColor
                    )
                    MacroBadge(
                        Vocabulary.get().fatsShort,
                        fats.truncate2Decimals().toString(),
                        fatsColor
                    )
                }
            }
        } else {
            Text(
                Vocabulary.get().pleaseSelectProduct,
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
            Text(Vocabulary.get().addToToday, color = Color.White)
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

    // Add state for multiplier
    var multiplier by remember { mutableStateOf(0f) }

    var previewCalories by remember { mutableFloatStateOf(0f) }
    var previewProtein by remember { mutableFloatStateOf(0f) }
    var previewCarbs by remember { mutableFloatStateOf(0f) }
    var previewFats by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(selectedMeal, quantity) {
        if (selectedMeal != null && quantity.toFloatOrNull() != null) {
            val quantityGrams = quantity.toFloat()
            val totalWeight = selectedMeal!!.products.sumOf { it.quantity.toDouble() }.toFloat()
            multiplier = if (totalWeight > 0) quantityGrams / totalWeight else 0f

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
            multiplier = 0f
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FilterableDropdown(
            options = meals,
            selectedItem = selectedMeal,
            onItemSelected = { meal ->
                selectedMeal = meal
            },
            label = Vocabulary.get().selectMeal,
            displayText = { it.meal.name },
            modifier = Modifier.fillMaxWidth()
        )

        if (selectedMeal != null) {
            Text(Vocabulary.get().mealContains, color = Color.White, fontWeight = FontWeight.Bold)
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
            Text(
                String.format(Vocabulary.get().totalMealWeight, totalWeight.truncate2Decimals()),
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            Input(
                label = Vocabulary.get().quantityConsumed,
                value = quantity,
                onChange = { quantity = it },
                keyboardType = KeyboardType.Number
            )

            val quantityFloat = quantity.toFloatOrNull()
            if (quantityFloat != null && quantityFloat > 0) {
                Text(
                    String.format(Vocabulary.get().servingFactor, multiplier.truncate2Decimals()),
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
                Text(
                    Vocabulary.get().noGoalSet,
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.W600
                )
                Text(
                    Vocabulary.get().recommendSetGoal,
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
                    Text(
                        Vocabulary.get().macroPreview,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MacroBadge(
                            Vocabulary.get().kcalShort,
                            previewCalories.truncate2Decimals().toString(),
                            caloriesColor
                        )
                        MacroBadge(
                            Vocabulary.get().proteinShort,
                            previewProtein.truncate2Decimals().toString(),
                            proteinColor
                        )
                        MacroBadge(
                            Vocabulary.get().carbsShort,
                            previewCarbs.truncate2Decimals().toString(),
                            carbsColor
                        )
                        MacroBadge(
                            Vocabulary.get().fatsShort,
                            previewFats.truncate2Decimals().toString(),
                            fatsColor
                        )
                    }
                }
            }
        } else {
            Text(
                Vocabulary.get().pleaseSelectMeal,
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
            Text(Vocabulary.get().addToToday, color = Color.White)
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