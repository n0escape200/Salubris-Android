package com.example.salubris.ui.screens.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.entities.MealComponent
import com.example.salubris.database.entities.ProductEntity
import com.example.salubris.database.viewmodels.MealUI
import com.example.salubris.database.viewmodels.MealViewModel
import com.example.salubris.database.viewmodels.ProductViewModel
import com.example.salubris.database.viewmodels.mealViewModelFactory
import com.example.salubris.database.viewmodels.productViewModelFactory
import com.example.salubris.ui.components.FilterableDropdown
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.ui.theme.cancelColor
import com.example.salubris.ui.theme.carbsColor
import com.example.salubris.ui.theme.fatsColor
import com.example.salubris.ui.theme.productColor
import com.example.salubris.ui.theme.proteinColor
import com.example.salubris.ui.theme.submitColor
import com.example.salubris.utils.ProductNutritionLabel
import com.example.salubris.utils.Vocabulary
import com.example.salubris.utils.truncate2Decimals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Meals() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val productViewModel: ProductViewModel = viewModel(factory = productViewModelFactory(context))
    val mealViewModel: MealViewModel = viewModel(factory = mealViewModelFactory(context))

    val products by productViewModel.products.collectAsStateWithLifecycle()
    val meals by mealViewModel.meals.collectAsStateWithLifecycle()
    val isLoading by mealViewModel.isLoading.collectAsStateWithLifecycle()

    // Normal add meal modal state
    var isOpen by remember { mutableStateOf(false) }
    var mealName by remember { mutableStateOf("") }
    val selectedComponents = remember { mutableStateListOf<MealComponent>() }

    // Hands‑free modal state
    var isHandsFreeOpen by remember { mutableStateOf(false) }
    val handsFreeComponents = remember { mutableStateListOf<MealComponent>() }
    val draftProducts = remember { mutableStateListOf<DraftProduct>() }

    // Edit meal state
    var editingMealUI by remember { mutableStateOf<MealUI?>(null) }
    var editComponents = remember { mutableStateListOf<MealComponent>() }
    var editName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        mealViewModel.loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { isOpen = true },
                    colors = ButtonDefaults.buttonColors(productColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(Vocabulary.get().addMeal, color = Color.White)
                }
                Button(
                    onClick = { isHandsFreeOpen = true },
                    colors = ButtonDefaults.buttonColors(caloriesColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(Vocabulary.get().handsFree, color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .background(ContainerBackground, RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .fillMaxSize()
            ) {
                if (isLoading) {
                    Text(Vocabulary.get().loading, color = Color.Gray)
                } else if (meals.isEmpty()) {
                    Text(Vocabulary.get().noMealsYet, color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(meals) { mealUI ->
                            MealItem(
                                mealUI = mealUI,
                                onEdit = {
                                    editingMealUI = mealUI
                                    editName = mealUI.meal.name
                                    editComponents.clear()
                                    editComponents.addAll(mealUI.components)
                                },
                                onDelete = {
                                    scope.launch {
                                        mealViewModel.deleteMeal(mealUI.meal)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Normal add meal dialog
        if (isOpen) {
            Dialog(
                onDismissRequest = { isOpen = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { isOpen = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
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
                                IconButton(onClick = { isOpen = false }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = Vocabulary.get().close,
                                        tint = Color.White
                                    )
                                }
                            }
                            NormalAddMealContent(
                                products = products,
                                selectedComponents = selectedComponents,
                                mealName = mealName,
                                onMealNameChange = { mealName = it },
                                productViewModel = productViewModel,
                                onSave = {
                                    if (mealName.isNotBlank() && selectedComponents.isNotEmpty()) {
                                        scope.launch {
                                            mealViewModel.addMeal(
                                                mealName,
                                                selectedComponents.toList()
                                            )
                                            isOpen = false
                                            mealName = ""
                                            selectedComponents.clear()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Edit meal dialog
        editingMealUI?.let { mealUI ->
            Dialog(
                onDismissRequest = { editingMealUI = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { editingMealUI = null },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
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
                                    "Edit Meal",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { editingMealUI = null }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = Vocabulary.get().close,
                                        tint = Color.White
                                    )
                                }
                            }
                            NormalAddMealContent(
                                products = products,
                                selectedComponents = editComponents,
                                mealName = editName,
                                onMealNameChange = { editName = it },
                                productViewModel = productViewModel,
                                onSave = {
                                    if (editName.isNotBlank() && editComponents.isNotEmpty()) {
                                        scope.launch {
                                            mealViewModel.updateMeal(
                                                mealUI.meal.uid,
                                                editName,
                                                editComponents.toList()
                                            )
                                            editingMealUI = null
                                            editComponents.clear()
                                            editName = ""
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Hands‑free dialog
        if (isHandsFreeOpen) {
            Dialog(
                onDismissRequest = { isHandsFreeOpen = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { isHandsFreeOpen = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .fillMaxHeight(0.9f)
                            .clickable { }
                            .background(Color(30, 30, 30), shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(30, 30, 30)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Vocabulary.get().handsFreeMealTitle,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { isHandsFreeOpen = false }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = Vocabulary.get().close,
                                        tint = Color.White
                                    )
                                }
                            }
                            HandsFreeMealContent(
                                isOpen = isHandsFreeOpen,
                                products = products,
                                initialHandsFreeComponents = handsFreeComponents,
                                initialDraftProducts = draftProducts,
                                onClose = {
                                    isHandsFreeOpen = false
                                    handsFreeComponents.clear()
                                    draftProducts.clear()
                                },
                                onSave = { name, finalComponents ->
                                    scope.launch {
                                        mealViewModel.addMeal(name, finalComponents)
                                        isHandsFreeOpen = false
                                        handsFreeComponents.clear()
                                        draftProducts.clear()
                                    }
                                },
                                productViewModel = productViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// MealItem – with edit support and draft badge
// ============================================================
@Composable
fun MealItem(mealUI: MealUI, onEdit: () -> Unit, onDelete: () -> Unit) {
    val hasDrafts = mealUI.components.any { it.isDraft }
    val totalWeight = mealUI.components.sumOf { it.quantity.toDouble() }.toFloat()
    var totalCalories = 0f
    var totalProtein = 0f
    var totalCarbs = 0f
    var totalFats = 0f

    mealUI.components.forEach { comp ->
        val factor = comp.quantity / 100f
        totalCalories += comp.calories * factor
        totalProtein += comp.protein * factor
        totalCarbs += comp.carbs * factor
        totalFats += comp.fats * factor
    }

    val per100Calories = if (totalWeight > 0) (totalCalories / totalWeight) * 100 else 0f
    val per100Protein = if (totalWeight > 0) (totalProtein / totalWeight) * 100 else 0f
    val per100Carbs = if (totalWeight > 0) (totalCarbs / totalWeight) * 100 else 0f
    val per100Fats = if (totalWeight > 0) (totalFats / totalWeight) * 100 else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (hasDrafts) Color(0xFF3D2B1F) else Color(60, 60, 60),
                RoundedCornerShape(5.dp)
            )
            .clickable { onEdit() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    mealUI.meal.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                if (hasDrafts) {
                    Spacer(Modifier.width(8.dp))
                    Text("⚠️", fontSize = 16.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val ingredientCount = mealUI.components.size
                Text(
                    if (ingredientCount == 1) {
                        "${ingredientCount} ${Vocabulary.get().ingredients}"
                    } else {
                        "${ingredientCount} ${Vocabulary.get().ingredientsPlural}"
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Text("•", color = Color.White.copy(alpha = 0.5f))
                Text(
                    "${totalWeight.truncate2Decimals()}g total",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "K: ${per100Calories.truncate2Decimals()}",
                    color = caloriesColor,
                    fontSize = 14.sp
                )
                Text(
                    "P: ${per100Protein.truncate2Decimals()}",
                    color = proteinColor,
                    fontSize = 14.sp
                )
                Text("C: ${per100Carbs.truncate2Decimals()}", color = carbsColor, fontSize = 14.sp)
                Text("F: ${per100Fats.truncate2Decimals()}", color = fatsColor, fontSize = 14.sp)
            }
            if (hasDrafts) {
                Text(
                    "Contains unresolved products – edit to resolve",
                    color = Color.Yellow,
                    fontSize = 11.sp
                )
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = cancelColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = Vocabulary.get().deleteMeal,
                tint = cancelColor,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

// ============================================================
// NormalAddMealContent – reusable for add and edit
// Now supports editing existing components (click to edit)
// ============================================================
@Composable
private fun NormalAddMealContent(
    products: List<ProductEntity>,
    selectedComponents: MutableList<MealComponent>,
    mealName: String,
    onMealNameChange: (String) -> Unit,
    productViewModel: ProductViewModel,
    onSave: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var quantityInput by remember { mutableStateOf("100") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var resolvingDraftIndex by remember { mutableStateOf<Int?>(null) }

    fun resetForm() {
        editingIndex = null
        selectedProduct = null
        quantityInput = "100"
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Input(
            label = Vocabulary.get().mealName,
            value = mealName,
            onChange = onMealNameChange,
            keyboardType = KeyboardType.Text
        )
        Text(Vocabulary.get().products, fontWeight = FontWeight.Bold, color = Color.White)

        selectedComponents.forEachIndexed { index, comp ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(55, 55, 55), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            comp.productName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (comp.isDraft) {
                            Spacer(Modifier.width(6.dp))
                            Text("⚠️", fontSize = 14.sp)
                        }
                    }
                    Text(
                        "${Vocabulary.get().quantityLabel} ${comp.quantity}g",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (comp.isDraft) {
                        Button(
                            onClick = { resolvingDraftIndex = index },
                            colors = ButtonDefaults.buttonColors(containerColor = productColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Resolve", color = Color.White, fontSize = 12.sp)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                editingIndex = index
                                selectedProduct = products.find { it.uid == comp.resolvedProductId }
                                quantityInput = comp.quantity.toString()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = productColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { selectedComponents.removeAt(index) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            Vocabulary.get().remove,
                            tint = cancelColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        FilterableDropdown(
            options = products,
            selectedItem = selectedProduct,
            onItemSelected = { selectedProduct = it },
            label = Vocabulary.get().selectProduct,
            displayText = { it.name },
            modifier = Modifier.fillMaxWidth()
        )
        Input(
            label = Vocabulary.get().quantityGrams,
            value = quantityInput,
            onChange = { quantityInput = it },
            keyboardType = KeyboardType.Number
        )
        if (selectedProduct != null) {
            ProductNutritionLabel(selectedProduct!!)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (editingIndex != null) {
                Button(
                    onClick = {
                        val qty = quantityInput.toFloatOrNull()
                        if (selectedProduct != null && qty != null && qty > 0) {
                            val newComp = MealComponent(
                                productName = selectedProduct!!.name,
                                calories = selectedProduct!!.calories,
                                protein = selectedProduct!!.protein,
                                carbs = selectedProduct!!.carbs,
                                fats = selectedProduct!!.fats,
                                quantity = qty,
                                resolvedProductId = selectedProduct!!.uid,
                                isDraft = false
                            )
                            selectedComponents[editingIndex!!] = newComp
                            resetForm()
                        }
                    },
                    enabled = selectedProduct != null && quantityInput.toFloatOrNull() != null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = productColor)
                ) {
                    Text("Update", color = Color.White)
                }
                Button(
                    onClick = { resetForm() },  // ✅ fixed: explicit lambda
                    colors = ButtonDefaults.buttonColors(containerColor = cancelColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        val qty = quantityInput.toFloatOrNull()
                        if (selectedProduct != null && qty != null && qty > 0) {
                            selectedComponents.add(
                                MealComponent(
                                    productName = selectedProduct!!.name,
                                    calories = selectedProduct!!.calories,
                                    protein = selectedProduct!!.protein,
                                    carbs = selectedProduct!!.carbs,
                                    fats = selectedProduct!!.fats,
                                    quantity = qty,
                                    resolvedProductId = selectedProduct!!.uid,
                                    isDraft = false
                                )
                            )
                            selectedProduct = null
                            quantityInput = "100"
                        }
                    },
                    enabled = selectedProduct != null && quantityInput.toFloatOrNull() != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = productColor)
                ) {
                    Text(Vocabulary.get().addProductButton, color = Color.White)
                }
            }
        }

        Button(
            onClick = onSave,
            enabled = mealName.isNotBlank() && selectedComponents.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = submitColor)
        ) {
            Text(Vocabulary.get().saveMeal, color = Color.White)
        }
    }

    if (resolvingDraftIndex != null) {
        val comp = selectedComponents[resolvingDraftIndex!!]
        val draft = DraftProduct(
            name = comp.productName,
            quantity = comp.quantity,
            resolvedProduct = null
        )
        ResolveDraftDialog(
            draft = draft,
            products = products,
            productViewModel = productViewModel,
            onResolved = { resolvedProduct ->
                val newComp = MealComponent(
                    productName = resolvedProduct.name,
                    calories = resolvedProduct.calories,
                    protein = resolvedProduct.protein,
                    carbs = resolvedProduct.carbs,
                    fats = resolvedProduct.fats,
                    quantity = draft.quantity,
                    resolvedProductId = resolvedProduct.uid,
                    isDraft = false
                )
                selectedComponents[resolvingDraftIndex!!] = newComp
                resolvingDraftIndex = null
            },
            onDismiss = { resolvingDraftIndex = null }
        )
    }
}

data class DraftProduct(
    val name: String,
    val quantity: Float,
    var resolvedProduct: ProductEntity? = null
)

// ============================================================
// HandsFreeMealContent – single combined list with resolve
// ============================================================
@Composable
fun HandsFreeMealContent(
    isOpen: Boolean,
    products: List<ProductEntity>,
    initialHandsFreeComponents: MutableList<MealComponent>,
    initialDraftProducts: MutableList<DraftProduct>,
    onClose: () -> Unit,
    onSave: (String, List<MealComponent>) -> Unit,
    productViewModel: ProductViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mealName by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var speechRecognizer: SpeechRecognizer? by remember { mutableStateOf(null) }
    var recognitionStatus by remember { mutableStateOf(Vocabulary.get().ready) }
    var permissionGranted by remember { mutableStateOf(false) }

    var resolvingDraftIndex by remember { mutableStateOf<Int?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (!isGranted) recognitionStatus = Vocabulary.get().microphonePermissionDenied
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        permissionGranted = hasPermission
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun parseCommand(input: String): Triple<String, Float, Boolean>? {
        val lower = input.lowercase().trim()
        if (!lower.startsWith("add ")) return null
        val rest = lower.removePrefix("add ").trim()
        val quantityPattern = Regex("""(\d+(?:\.\d+)?)\s*(g|gram|grams)?$""")
        val match = quantityPattern.find(rest)
        val productName = if (match != null) {
            rest.substring(0, match.range.first).trim()
        } else {
            rest
        }
        val quantity = match?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
        return Triple(productName, quantity, quantity == 0f)
    }

    fun handleVoiceCommand(command: String) {
        val parsed = parseCommand(command)
        if (parsed == null) {
            recognitionStatus = Vocabulary.get().commandNotRecognized
            return
        }
        val (productName, quantity, needsQuantity) = parsed
        if (productName.isBlank()) {
            recognitionStatus = Vocabulary.get().noProductName
            return
        }
        if (needsQuantity) {
            recognitionStatus = Vocabulary.get().pleaseSayQuantity.replace("{product}", productName)
            return
        }
        val existing = products.find { it.name.equals(productName, ignoreCase = true) }
        if (existing != null) {
            initialHandsFreeComponents.add(
                MealComponent(
                    productName = existing.name,
                    calories = existing.calories,
                    protein = existing.protein,
                    carbs = existing.carbs,
                    fats = existing.fats,
                    quantity = quantity,
                    resolvedProductId = existing.uid,
                    isDraft = false
                )
            )
            recognitionStatus =
                Vocabulary.get().addedProduct.replace("{quantity}", quantity.toString())
                    .replace("{product}", existing.name)
        } else {
            initialHandsFreeComponents.add(
                MealComponent(
                    productName = productName,
                    calories = 0f,
                    protein = 0f,
                    carbs = 0f,
                    fats = 0f,
                    quantity = quantity,
                    resolvedProductId = null,
                    isDraft = true
                )
            )
            recognitionStatus =
                Vocabulary.get().productNotFoundAddedDraft.replace("{product}", productName)
        }
    }

    fun startListening() {
        if (!permissionGranted) return
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    recognitionStatus = Vocabulary.get().listening
                }

                override fun onBeginningOfSpeech() {
                    recognitionStatus = Vocabulary.get().speaking
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    recognitionStatus = Vocabulary.get().processing
                }

                override fun onError(error: Int) {
                    isListening = false
                    recognitionStatus = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> Vocabulary.get().noCommandRecognized
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> Vocabulary.get().recognizerBusy
                        else -> Vocabulary.get().errorWithCode.replace("{error}", error.toString())
                    }
                    if (permissionGranted && isOpen) {
                        scope.launch {
                            delay(500)
                            startListening()
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()
                    if (spoken != null) {
                        handleVoiceCommand(spoken)
                    } else {
                        recognitionStatus = Vocabulary.get().couldNotUnderstand
                    }
                    if (permissionGranted && isOpen) {
                        scope.launch {
                            delay(500)
                            startListening()
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        if (!isListening && permissionGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, Vocabulary.get().speechPrompt)
            }
            speechRecognizer?.startListening(intent)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }

    LaunchedEffect(isOpen, permissionGranted) {
        if (isOpen && permissionGranted) {
            startListening()
        } else {
            stopListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose { stopListening() }
    }

    fun resolveDraft(index: Int, resolvedProduct: ProductEntity) {
        val comp = initialHandsFreeComponents[index]
        val newComp = MealComponent(
            productName = resolvedProduct.name,
            calories = resolvedProduct.calories,
            protein = resolvedProduct.protein,
            carbs = resolvedProduct.carbs,
            fats = resolvedProduct.fats,
            quantity = comp.quantity,
            resolvedProductId = resolvedProduct.uid,
            isDraft = false
        )
        initialHandsFreeComponents[index] = newComp
        recognitionStatus =
            Vocabulary.get().resolvedProduct.replace("{product}", resolvedProduct.name)
        if (permissionGranted && isOpen) {
            scope.launch {
                delay(500)
                startListening()
            }
        }
    }

    val totalCalories =
        initialHandsFreeComponents.map { (it.calories * it.quantity) / 100f }.sum()
    val totalProtein =
        initialHandsFreeComponents.map { (it.protein * it.quantity) / 100f }.sum()
    val totalCarbs = initialHandsFreeComponents.map { (it.carbs * it.quantity) / 100f }.sum()
    val totalFats = initialHandsFreeComponents.map { (it.fats * it.quantity) / 100f }.sum()
    val totalWeight = initialHandsFreeComponents.map { it.quantity }.sum()

    Column(modifier = Modifier.padding(8.dp)) {
        Input(
            label = Vocabulary.get().mealName,
            value = mealName,
            onChange = { mealName = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = if (isListening) Vocabulary.get().listening else Vocabulary.get().notListening,
                tint = if (isListening) submitColor else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                recognitionStatus,
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Check,
                contentDescription = Vocabulary.get().resolved,
                tint = submitColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${Vocabulary.get().addedProducts} (${initialHandsFreeComponents.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        if (initialHandsFreeComponents.isEmpty()) {
            Text(Vocabulary.get().noProductsAdded, color = Color.Gray, fontSize = 14.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                initialHandsFreeComponents.forEachIndexed { index, p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (p.isDraft) Color(60, 40, 20) else Color(50, 50, 50),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${p.productName} – ${p.quantity}g", color = Color.White)
                            if (p.isDraft) {
                                Spacer(Modifier.width(6.dp))
                                Text("⚠️", fontSize = 14.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (p.isDraft) {
                                Button(
                                    onClick = { resolvingDraftIndex = index },
                                    colors = ButtonDefaults.buttonColors(containerColor = productColor),
                                    contentPadding = PaddingValues(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                                ) {
                                    Text("Resolve", color = Color.White, fontSize = 12.sp)
                                }
                            }
                            IconButton(
                                onClick = { initialHandsFreeComponents.removeAt(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = Vocabulary.get().remove,
                                    tint = cancelColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                if (totalWeight > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            Vocabulary.get().totals,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        Text(
                            "K: ${totalCalories.truncate2Decimals()}",
                            color = caloriesColor,
                            fontSize = 13.sp
                        )
                        Text(
                            "P: ${totalProtein.truncate2Decimals()}",
                            color = proteinColor,
                            fontSize = 13.sp
                        )
                        Text(
                            "C: ${totalCarbs.truncate2Decimals()}",
                            color = carbsColor,
                            fontSize = 13.sp
                        )
                        Text(
                            "F: ${totalFats.truncate2Decimals()}",
                            color = fatsColor,
                            fontSize = 13.sp
                        )
                        Text(
                            "(${totalWeight.truncate2Decimals()}g)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = cancelColor),
                modifier = Modifier.weight(1f)
            ) {
                Text(Vocabulary.get().cancel)
            }
            Button(
                onClick = {
                    if (mealName.isBlank()) {
                        recognitionStatus = Vocabulary.get().pleaseEnterMealName
                        return@Button
                    }
                    onSave(mealName, initialHandsFreeComponents.toList())
                },
                colors = ButtonDefaults.buttonColors(containerColor = productColor),
                modifier = Modifier.weight(1f),
                enabled = mealName.isNotBlank() && initialHandsFreeComponents.isNotEmpty()
            ) {
                Text(Vocabulary.get().saveMeal)
            }
        }

        if (resolvingDraftIndex != null) {
            val comp = initialHandsFreeComponents[resolvingDraftIndex!!]
            val draft = DraftProduct(
                name = comp.productName,
                quantity = comp.quantity,
                resolvedProduct = null
            )
            ResolveDraftDialog(
                draft = draft,
                products = products,
                productViewModel = productViewModel,
                onResolved = { resolvedProduct ->
                    resolveDraft(resolvingDraftIndex!!, resolvedProduct)
                    resolvingDraftIndex = null
                },
                onDismiss = { resolvingDraftIndex = null }
            )
        }
    }
}

// ============================================================
// ResolveDraftDialog – fixed theming
// ============================================================
@Composable
private fun ResolveDraftDialog(
    draft: DraftProduct,
    products: List<ProductEntity>,
    productViewModel: ProductViewModel,
    onResolved: (ProductEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedExistingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var newProductName by remember { mutableStateOf(draft.name) }
    var newCalories by remember { mutableStateOf("") }
    var newProtein by remember { mutableStateOf("") }
    var newCarbs by remember { mutableStateOf("") }
    var newFats by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ContainerBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = Vocabulary.get().warning,
                    tint = Color.Yellow
                )
                Spacer(Modifier.width(8.dp))
                Text("${Vocabulary.get().resolveDraftTitle}${draft.name}", color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = Vocabulary.get().link,
                        tint = productColor
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        Vocabulary.get().mapExisting,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                FilterableDropdown(
                    options = products,
                    selectedItem = selectedExistingProduct,
                    onItemSelected = { selectedExistingProduct = it },
                    label = Vocabulary.get().selectProduct,
                    displayText = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = Vocabulary.get().createNew,
                        tint = submitColor
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        Vocabulary.get().createNew,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Input(
                    Vocabulary.get().productName,
                    newProductName,
                    onChange = { newProductName = it })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Input(
                            Vocabulary.get().caloriesPer100g,
                            newCalories,
                            onChange = { newCalories = it },
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Input(
                            Vocabulary.get().proteinPer100g,
                            newProtein,
                            onChange = { newProtein = it },
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Input(
                            Vocabulary.get().carbsPer100g,
                            newCarbs,
                            onChange = { newCarbs = it },
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Input(
                            Vocabulary.get().fatsPer100g,
                            newFats,
                            onChange = { newFats = it },
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val resolved = when {
                        selectedExistingProduct != null -> selectedExistingProduct!!
                        else -> {
                            val product = ProductEntity(
                                name = newProductName.ifBlank { draft.name },
                                calories = newCalories.toFloatOrNull() ?: 0f,
                                protein = newProtein.toFloatOrNull() ?: 0f,
                                carbs = newCarbs.toFloatOrNull() ?: 0f,
                                fats = newFats.toFloatOrNull() ?: 0f
                            )
                            scope.launch { productViewModel.addProduct(product) }
                            product
                        }
                    }
                    onResolved(resolved)
                },
                colors = ButtonDefaults.textButtonColors(contentColor = productColor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = productColor
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(Vocabulary.get().confirm, color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = cancelColor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = cancelColor
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(Vocabulary.get().cancel, color = Color.White)
                }
            }
        }
    )
}