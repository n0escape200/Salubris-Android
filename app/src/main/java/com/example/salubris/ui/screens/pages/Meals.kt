package com.example.salubris.ui.screens.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.salubris.database.entities.MealWithProducts
import com.example.salubris.database.entities.Product
import com.example.salubris.database.relations.ProductWithQuantity
import com.example.salubris.database.viewmodels.MealViewModel
import com.example.salubris.database.viewmodels.ProductViewModel
import com.example.salubris.database.viewmodels.mealViewModelFactory
import com.example.salubris.database.viewmodels.productViewModelFactory
import com.example.salubris.ui.components.FilterableDropdown
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.theme.*
import com.example.salubris.utils.ProductNutritionLabel
import com.example.salubris.utils.calculateMacrosForProduct
import com.example.salubris.utils.truncate2Decimals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

// Helper beep function (top level)
private fun playBeep() {
    try {
        val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        tone.release()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Meals() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val productViewModel: ProductViewModel = viewModel(factory = productViewModelFactory(context))
    val mealViewModel: MealViewModel = viewModel(factory = mealViewModelFactory(context))

    val products by productViewModel.products.collectAsStateWithLifecycle()
    val mealsWithProducts by mealViewModel.mealsWithProducts.collectAsStateWithLifecycle()
    val isLoading by mealViewModel.isLoading.collectAsStateWithLifecycle()

    // Normal add meal modal state
    var isOpen by remember { mutableStateOf(false) }
    var mealName by remember { mutableStateOf("") }
    val selectedProducts = remember { mutableStateListOf<ProductWithQuantity>() }

    // Hands‑free modal state
    var isHandsFreeOpen by remember { mutableStateOf(false) }
    val handsFreeProducts = remember { mutableStateListOf<ProductWithQuantity>() }
    val draftProducts = remember { mutableStateListOf<DraftProduct>() }

    LaunchedEffect(Unit) {
        mealViewModel.loadData()
    }

    Box(modifier = Modifier.fillMaxSize().padding(5.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { isOpen = true },
                    colors = ButtonDefaults.buttonColors(productColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Meal", color = Color.White)
                }
                Button(
                    onClick = { isHandsFreeOpen = true },
                    colors = ButtonDefaults.buttonColors(caloriesColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hands‑free", color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .background(ContainerBackground, RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .fillMaxSize()
            ) {
                if (isLoading) {
                    Text("Loading...", color = Color.Gray)
                } else if (mealsWithProducts.isEmpty()) {
                    Text("No meals yet", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(mealsWithProducts) { mealWithProducts ->
                            MealItem(
                                mealWithProducts = mealWithProducts,
                                onDelete = {
                                    scope.launch {
                                        mealViewModel.deleteMeal(mealWithProducts.meal)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Normal add meal dialog (unchanged)
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
                                IconButton(onClick = { isOpen = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                            NormalAddMealContent(
                                products = products,
                                selectedProducts = selectedProducts,
                                mealName = mealName,
                                onMealNameChange = { mealName = it },
                                productViewModel = productViewModel,
                                onSave = {
                                    if (mealName.isNotBlank() && selectedProducts.isNotEmpty()) {
                                        scope.launch {
                                            mealViewModel.addMeal(mealName, selectedProducts.toList())
                                            isOpen = false
                                            mealName = ""
                                            selectedProducts.clear()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Hands‑free dialog (continuous listening)
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
                                Text("Hands‑free Meal", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { isHandsFreeOpen = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                            HandsFreeMealContent(
                                isOpen = isHandsFreeOpen,
                                products = products,
                                initialHandsFreeProducts = handsFreeProducts,
                                initialDraftProducts = draftProducts,
                                onClose = {
                                    isHandsFreeOpen = false
                                    handsFreeProducts.clear()
                                    draftProducts.clear()
                                },
                                onSave = { name, finalProducts ->
                                    scope.launch {
                                        mealViewModel.addMeal(name, finalProducts)
                                        isHandsFreeOpen = false
                                        handsFreeProducts.clear()
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
// MealItem
// ============================================================
@Composable
fun MealItem(mealWithProducts: MealWithProducts, onDelete: () -> Unit) {
    val totalWeight = mealWithProducts.products.sumOf { it.quantity.toDouble() }.toFloat()
    var totalCalories = 0f
    var totalProtein = 0f
    var totalCarbs = 0f
    var totalFats = 0f

    mealWithProducts.products.forEach { productWithQty ->
        val macros = calculateMacrosForProduct(productWithQty.product, productWithQty.quantity)
        totalCalories += macros["calories"] ?: 0f
        totalProtein += macros["protein"] ?: 0f
        totalCarbs += macros["carbs"] ?: 0f
        totalFats += macros["fats"] ?: 0f
    }

    val per100Calories = if (totalWeight > 0) (totalCalories / totalWeight) * 100 else 0f
    val per100Protein = if (totalWeight > 0) (totalProtein / totalWeight) * 100 else 0f
    val per100Carbs = if (totalWeight > 0) (totalCarbs / totalWeight) * 100 else 0f
    val per100Fats = if (totalWeight > 0) (totalFats / totalWeight) * 100 else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(60, 60, 60), RoundedCornerShape(5.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                mealWithProducts.meal.name,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${mealWithProducts.products.size} ingredient${if (mealWithProducts.products.size != 1) "s" else ""}",
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
                Text("K: ${per100Calories.truncate2Decimals()}", color = caloriesColor, fontSize = 14.sp)
                Text("P: ${per100Protein.truncate2Decimals()}", color = proteinColor, fontSize = 14.sp)
                Text("C: ${per100Carbs.truncate2Decimals()}", color = carbsColor, fontSize = 14.sp)
                Text("F: ${per100Fats.truncate2Decimals()}", color = fatsColor, fontSize = 14.sp)
            }
            Text("per 100g", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
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
                contentDescription = "Delete meal",
                tint = cancelColor,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

// ============================================================
// NormalAddMealContent
// ============================================================
@Composable
private fun NormalAddMealContent(
    products: List<Product>,
    selectedProducts: MutableList<ProductWithQuantity>,
    mealName: String,
    onMealNameChange: (String) -> Unit,
    productViewModel: ProductViewModel,
    onSave: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityInput by remember { mutableStateOf("100") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Input(
            label = "Meal name",
            value = mealName,
            onChange = onMealNameChange,
            keyboardType = KeyboardType.Text
        )
        Text("Products", fontWeight = FontWeight.Bold, color = Color.White)
        selectedProducts.forEachIndexed { index, productWithQty ->
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(55,55,55), RoundedCornerShape(12.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(productWithQty.product.name, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Quantity: ${productWithQty.quantity}g", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                }
                IconButton(onClick = { selectedProducts.removeAt(index) }) {
                    Icon(Icons.Default.Delete, "Remove", tint = cancelColor, modifier = Modifier.size(24.dp))
                }
            }
        }
        FilterableDropdown(
            options = products,
            selectedItem = selectedProduct,
            onItemSelected = { selectedProduct = it },
            label = "Select a product",
            displayText = { it.name },
            modifier = Modifier.fillMaxWidth()
        )
        Input(
            label = "Quantity (g)",
            value = quantityInput,
            onChange = { quantityInput = it },
            keyboardType = KeyboardType.Number
        )
        if (selectedProduct != null) {
            ProductNutritionLabel(selectedProduct!!)
        }
        Button(
            onClick = {
                val qty = quantityInput.toFloatOrNull()
                if (selectedProduct != null && qty != null && qty > 0) {
                    selectedProducts.add(ProductWithQuantity(selectedProduct!!, qty))
                    selectedProduct = null
                    quantityInput = "100"
                }
            },
            enabled = selectedProduct != null && quantityInput.toFloatOrNull() != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = productColor)
        ) {
            Text("ADD PRODUCT", color = Color.White)
        }
        Button(
            onClick = onSave,
            enabled = mealName.isNotBlank() && selectedProducts.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = submitColor)
        ) {
            Text("SAVE MEAL", color = Color.White)
        }
    }
}

data class DraftProduct(
    val name: String,
    val quantity: Float,
    var resolvedProduct: Product? = null
)

@Composable
fun HandsFreeMealContent(
    isOpen: Boolean,
    products: List<Product>,
    initialHandsFreeProducts: MutableList<ProductWithQuantity>,
    initialDraftProducts: MutableList<DraftProduct>,
    onClose: () -> Unit,
    onSave: (String, List<ProductWithQuantity>) -> Unit,
    productViewModel: ProductViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mealName by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var speechRecognizer: SpeechRecognizer? by remember { mutableStateOf(null) }
    var recognitionStatus by remember { mutableStateOf("Ready") }
    var permissionGranted by remember { mutableStateOf(false) }
    var isRestarting by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (!isGranted) recognitionStatus = "Microphone permission denied"
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
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
            recognitionStatus = "Command not recognized (start with 'add')"
            return
        }
        val (productName, quantity, needsQuantity) = parsed
        if (productName.isBlank()) {
            recognitionStatus = "No product name"
            return
        }
        if (needsQuantity) {
            recognitionStatus = "Please say quantity for $productName (e.g., '100 grams')"
            return
        }
        val existing = products.find { it.name.equals(productName, ignoreCase = true) }
        if (existing != null) {
            initialHandsFreeProducts.add(ProductWithQuantity(existing, quantity))
            recognitionStatus = "Added ${quantity}g of ${existing.name}"
        } else {
            val draft = DraftProduct(productName, quantity)
            initialDraftProducts.add(draft)
            recognitionStatus = "$productName not found. Resolve in dialog."
        }
    }

    fun startContinuousListening() {
        if (!permissionGranted || isRestarting) return
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    recognitionStatus = "Listening..."
                }
                override fun onBeginningOfSpeech() { recognitionStatus = "Speaking..." }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { recognitionStatus = "Processing..." }
                override fun onError(error: Int) {
                    isListening = false
                    recognitionStatus = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No command recognized"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                        else -> "Error: $error"
                    }
                    if (permissionGranted && isOpen && !isRestarting) {
                        scope.launch {
                            isRestarting = true
                            delay(1500)
                            isRestarting = false
                            startContinuousListening()
                        }
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()
                    if (spoken != null) {
                        handleVoiceCommand(spoken)
                    } else {
                        recognitionStatus = "Could not understand"
                    }
                    if (permissionGranted && isOpen && !isRestarting) {
                        scope.launch {
                            isRestarting = true
                            delay(500)
                            isRestarting = false
                            startContinuousListening()
                        }
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'add product quantity'")
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }

    LaunchedEffect(isOpen, permissionGranted) {
        if (isOpen && permissionGranted) {
            startContinuousListening()
        } else {
            stopListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose { stopListening() }
    }

    Column(modifier = Modifier.padding(8.dp)) {
        Text("Hands‑free meal creation", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Input("Meal name", value = mealName, onChange = { mealName = it })
        Spacer(Modifier.height(8.dp))

        Text("Added products:", color = Color.White)
        LazyColumn(modifier = Modifier.height(200.dp)) {
            items(initialHandsFreeProducts) { p ->
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    Text("${p.product.name} - ${p.quantity}g", color = Color.White)
                }
            }
            items(initialDraftProducts) { draft ->
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    Text("⚠️ ${draft.name} - ${draft.quantity}g (unresolved)", color = Color.Yellow)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = if (isListening) "Listening" else "Not listening",
                tint = if (isListening) submitColor else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Text(recognitionStatus, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onClose, colors = ButtonDefaults.buttonColors(cancelColor)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (initialDraftProducts.isNotEmpty()) {
                        recognitionStatus = "Please resolve all unknown products first"
                        return@Button
                    }
                    if (mealName.isBlank()) {
                        recognitionStatus = "Please enter a meal name"
                        return@Button
                    }
                    if (initialHandsFreeProducts.isEmpty()) {
                        recognitionStatus = "No products added"
                        return@Button
                    }
                    onSave(mealName, initialHandsFreeProducts.toList())
                },
                colors = ButtonDefaults.buttonColors(productColor)
            ) {
                Text("Save Meal")
            }
        }
    }

    if (initialDraftProducts.isNotEmpty()) {
        val currentDraft = initialDraftProducts.first()
        ResolveDraftDialog(
            draft = currentDraft,
            products = products,
            productViewModel = productViewModel,
            onResolved = { resolvedProduct ->
                initialHandsFreeProducts.add(ProductWithQuantity(resolvedProduct, currentDraft.quantity))
                initialDraftProducts.remove(currentDraft)
                recognitionStatus = "Resolved: ${resolvedProduct.name}"
            }
        )
    }
}

@Composable
private fun ResolveDraftDialog(
    draft: DraftProduct,
    products: List<Product>,
    productViewModel: ProductViewModel,
    onResolved: (Product) -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    var selectedExistingProduct by remember { mutableStateOf<Product?>(null) }
    var newProductName by remember { mutableStateOf(draft.name) }
    var newCalories by remember { mutableStateOf("") }
    var newProtein by remember { mutableStateOf("") }
    var newCarbs by remember { mutableStateOf("") }
    var newFats by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Unknown product: ${draft.name}", color = Color.White) },
            text = {
                Column {
                    Text("Choose action:", color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    FilterableDropdown(
                        options = products,
                        selectedItem = selectedExistingProduct,
                        onItemSelected = { selectedExistingProduct = it },
                        label = "Map to existing product",
                        displayText = { it.name }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("OR create new product", color = Color.White)
                    Input("Product name", newProductName, onChange = { newProductName = it })
                    Input("Calories per 100g", newCalories, onChange = { newCalories = it }, keyboardType = KeyboardType.Decimal)
                    Input("Protein per 100g", newProtein, onChange = { newProtein = it }, keyboardType = KeyboardType.Decimal)
                    Input("Carbs per 100g", newCarbs, onChange = { newCarbs = it }, keyboardType = KeyboardType.Decimal)
                    Input("Fats per 100g", newFats, onChange = { newFats = it }, keyboardType = KeyboardType.Decimal)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val resolved = when {
                            selectedExistingProduct != null -> selectedExistingProduct!!
                            else -> {
                                val product = Product(
                                    name = newProductName,
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
                        showDialog = false
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}