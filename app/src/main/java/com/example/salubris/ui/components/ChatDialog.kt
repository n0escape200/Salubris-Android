package com.example.salubris.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arm.aichat.InferenceEngine
import com.arm.aichat.InferenceEngine.State
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.Product
import com.example.salubris.database.repositories.ProductRepository
import com.example.salubris.nutrition.*
import com.example.salubris.ui.theme.*
import com.example.salubris.utils.Vocabulary
import com.example.salubris.utils.toFloatSafe
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log

val State.isModelLoaded: Boolean
    get() = this is State.ModelReady ||
            this is State.Benchmarking ||
            this is State.ProcessingSystemPrompt ||
            this is State.ProcessingUserPrompt ||
            this is State.Generating

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false,
    val sourceUrl: String? = null,
    val sourceLabel: String? = null,
    val nutritionData: FoodNutritionData? = null
)

private fun extractCoreFoodName(prompt: String): String {
    val stopWords = setOf(
        "give", "me", "the", "for", "per", "of", "in", "a", "an", "is", "what", "how", "much",
        "many", "macros", "macro", "nutrition", "nutritional", "calories", "calorie",
        "kcal", "protein", "proteins", "fat", "fats", "carbs", "carbohydrates",
        "100g", "100", "g", "grams", "gram", "kg", "ml", "per", "100"
    )
    val regex = Regex("\\b(\\d+)\\s?(g|grams|gram|kg|ml)\\b")
    return prompt
        .replace(regex, "")
        .split(" ")
        .filter { it.lowercase() !in stopWords && it.isNotBlank() }
        .joinToString(" ")
        .trim()
}

private fun formatSingleFoodResponse(data: FoodNutritionData): String {
    return """
        ${data.name}
        
        Calories: ${"%.1f".format(data.calories)} kcal/100g
        Protein: ${"%.1f".format(data.protein)} g/100g
        Carbs: ${"%.1f".format(data.carbs)} g/100g
        Fat: ${"%.1f".format(data.fat)} g/100g
        ${if (data.fiber != null) "Fiber: ${"%.1f".format(data.fiber)} g/100g\n" else ""}
        ${if (data.sugar != null) "Sugars: ${"%.1f".format(data.sugar)} g/100g\n" else ""}
        Source: ${data.sourceName}
    """.trimIndent()
}

private fun formatMealResponse(
    items: List<FoodNutritionData>,
    totalCal: Double,
    totalProtein: Double,
    totalCarbs: Double,
    totalFat: Double
): String {
    val sb = StringBuilder()
    sb.appendLine("Meal Summary")
    sb.appendLine()
    items.forEach { data ->
        sb.appendLine("${data.name} (scaled):")
        sb.appendLine("  Calories: ${"%.1f".format(data.calories)} kcal")
        sb.appendLine("  Protein: ${"%.1f".format(data.protein)} g")
        sb.appendLine("  Carbs: ${"%.1f".format(data.carbs)} g")
        sb.appendLine("  Fat: ${"%.1f".format(data.fat)} g")
        sb.appendLine()
    }
    sb.appendLine("Total:")
    sb.appendLine("  Calories: ${"%.1f".format(totalCal)} kcal")
    sb.appendLine("  Protein: ${"%.1f".format(totalProtein)} g")
    sb.appendLine("  Carbs: ${"%.1f".format(totalCarbs)} g")
    sb.appendLine("  Fat: ${"%.1f".format(totalFat)} g")
    if (items.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine("Sources:")
        items.forEach { data ->
            sb.appendLine("- ${data.sourceName} (${data.sourceUrl})")
        }
    }
    return sb.toString()
}

private fun extractQuantity(prompt: String): Double {
    val regex = Regex("""(\d+(?:\.\d+)?)\s?(g|grams|gram|gm|gr)\b""", RegexOption.IGNORE_CASE)
    val match = regex.find(prompt)
    return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 100.0
}

private fun extractRequestedNutrients(prompt: String): Set<String> {
    val lower = prompt.lowercase()
    val nutrients = mutableSetOf<String>()
    if (lower.contains("calorie") || lower.contains("kcal")) nutrients.add("calories")
    if (lower.contains("protein")) nutrients.add("protein")
    if (lower.contains("carbs") || lower.contains("carbohydrate")) nutrients.add("carbs")
    if (lower.contains("fat")) nutrients.add("fat")
    if (lower.contains("fiber")) nutrients.add("fiber")
    if (lower.contains("sugar")) nutrients.add("sugar")
    if (nutrients.isEmpty()) nutrients.addAll(listOf("calories", "protein", "carbs", "fat"))
    return nutrients
}

@Composable
fun ChatDialog(
    engine: InferenceEngine,
    modelPath: String,
    webSearchService: WebSearchService,
    onDismiss: () -> Unit
) {
    val engineState by engine.state.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ContainerBackground)
        ) {
            when {
                engineState is State.Error ->
                    ErrorContent((engineState as State.Error).exception, onDismiss)

                engineState.isModelLoaded ->
                    ChatContent(engine, webSearchService, onDismiss)

                else ->
                    LoadingContent()
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = productColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text(Vocabulary.get().aiAssistantLoading, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
private fun ErrorContent(exception: Exception, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("😔", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            Vocabulary.get().failedToLoadModel,
            color = cancelColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            exception.message ?: Vocabulary.get().unknownError,
            color = Color.LightGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = productColor)
        ) {
            Text(Vocabulary.get().close, color = Color.White)
        }
    }
}

@Composable
private fun ChatContent(
    engine: InferenceEngine,
    webSearchService: WebSearchService,
    onDismiss: () -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var isGenerating by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var productFormData by remember { mutableStateOf<FoodNutritionData?>(null) }
    val database = AppDatabase.getDatabase(context)
    val productRepository = remember { ProductRepository(database.productDao()) }

    LaunchedEffect(Unit) {
        try {
            engine.setSystemPrompt(
                "You are a nutrition assistant for the Salubris app. " +
                        "You answer general nutrition and health questions concisely (no more than 4 sentences). " +
                        "Never invent numbers. " +
                        "If the user asks something unrelated to nutrition, politely say you can only help with nutrition and suggest typing 'Help' for examples. " +
                        "Do not use markdown. " +
                        "Do not output any thinking, analysis, planning, or self‑correction. " +
                        "Output ONLY the final plain‑text answer."
            )
        } catch (_: Exception) {
        }
        messages.add(
            ChatMessage(
                Vocabulary.get().chatGreeting,
                isUser = false
            )
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(productColor.copy(alpha = 0.9f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    Vocabulary.get().aiAssistant,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, Vocabulary.get().close, tint = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    onAddToProducts = { data -> productFormData = data }
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ContainerBackground,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            Vocabulary.get().askMeAnything,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = productColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = productColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    maxLines = 4,
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val prompt = userInput.trim()
                        if (prompt.isNotEmpty() && !isGenerating) {
                            messages.add(ChatMessage(prompt, isUser = true))
                            userInput = ""
                            isGenerating = true

                            val loadingMsg = ChatMessage("", isUser = false, isLoading = true)
                            messages.add(loadingMsg)
                            val loadingIndex = messages.lastIndex

                            scope.launch {
                                try {
                                    if (prompt.equals("help", ignoreCase = true)) {
                                        updateMessage(
                                            messages,
                                            loadingIndex,
                                            ChatMessage(Vocabulary.get().chatHelpGuide, false)
                                        )
                                        return@launch
                                    }

                                    val intent = NutritionIntentDetector.detect(prompt)

                                    when (intent) {
                                        NutritionIntent.SINGLE_FOOD -> {
                                            val quantity = extractQuantity(prompt)
                                            val nutrients = extractRequestedNutrients(prompt)
                                            val foodName = extractCoreFoodName(prompt)
                                            val query = foodName.ifBlank { prompt }
                                            val results = webSearchService.searchFood(query)

                                            if (results.isNotEmpty()) {
                                                val data = results.first()
                                                val scale = quantity / 100.0
                                                val formatted = buildString {
                                                    appendLine(data.name)
                                                    appendLine()
                                                    if ("calories" in nutrients) appendLine(
                                                        "Calories: ${
                                                            "%.1f".format(
                                                                data.calories * scale
                                                            )
                                                        } kcal"
                                                    )
                                                    if ("protein" in nutrients) appendLine(
                                                        "Protein: ${
                                                            "%.1f".format(
                                                                data.protein * scale
                                                            )
                                                        } g"
                                                    )
                                                    if ("carbs" in nutrients) appendLine(
                                                        "Carbs: ${
                                                            "%.1f".format(
                                                                data.carbs * scale
                                                            )
                                                        } g"
                                                    )
                                                    if ("fat" in nutrients) appendLine(
                                                        "Fat: ${
                                                            "%.1f".format(
                                                                data.fat * scale
                                                            )
                                                        } g"
                                                    )
                                                    if ("fiber" in nutrients && data.fiber != null) appendLine(
                                                        "Fiber: ${"%.1f".format(data.fiber * scale)} g"
                                                    )
                                                    if ("sugar" in nutrients && data.sugar != null) appendLine(
                                                        "Sugars: ${"%.1f".format(data.sugar * scale)} g"
                                                    )
                                                    if (quantity != 100.0) appendLine()
                                                    append("Source: ${data.sourceName}")
                                                }
                                                updateMessage(
                                                    messages, loadingIndex,
                                                    ChatMessage(
                                                        text = formatted.trimEnd(),
                                                        isUser = false,
                                                        sourceUrl = data.sourceUrl,
                                                        sourceLabel = data.sourceName,
                                                        nutritionData = data
                                                    )
                                                )
                                            } else {
                                                updateMessage(
                                                    messages, loadingIndex,
                                                    ChatMessage(
                                                        Vocabulary.get().noReliableData,
                                                        false
                                                    )
                                                )
                                            }
                                        }

                                        NutritionIntent.MULTI_FOOD -> {
                                            updateMessage(
                                                messages, loadingIndex,
                                                ChatMessage(
                                                    Vocabulary.get().chatPromptForQuantities,
                                                    false
                                                )
                                            )
                                        }

                                        NutritionIntent.MEAL -> {
                                            val mealRegex =
                                                Regex("""(\d+)\s?(g|grams|gram|kg|ml)\s+([a-zA-Z ]+)""")
                                            val items = mealRegex.findAll(prompt).map { match ->
                                                val grams = match.groupValues[1].toDouble()
                                                val food = match.groupValues[3].trim()
                                                MealItem(name = food, grams = grams)
                                            }.toList()

                                            if (items.isEmpty()) {
                                                updateMessage(
                                                    messages, loadingIndex,
                                                    ChatMessage(
                                                        Vocabulary.get().chatInvalidQuantityFormat,
                                                        false
                                                    )
                                                )
                                            } else {
                                                val allFoodData = mutableListOf<FoodNutritionData>()
                                                for (item in items) {
                                                    val results =
                                                        webSearchService.searchFood(item.name)
                                                    if (results.isNotEmpty()) {
                                                        val per100g = results.first()
                                                        val factor = item.grams / 100.0
                                                        allFoodData.add(
                                                            per100g.copy(
                                                                calories = per100g.calories * factor,
                                                                protein = per100g.protein * factor,
                                                                carbs = per100g.carbs * factor,
                                                                fat = per100g.fat * factor,
                                                                fiber = per100g.fiber?.let { it * factor },
                                                                sugar = per100g.sugar?.let { it * factor }
                                                            ))
                                                    }
                                                }

                                                if (allFoodData.isEmpty()) {
                                                    updateMessage(
                                                        messages, loadingIndex,
                                                        ChatMessage(
                                                            Vocabulary.get().chatNoDataForIngredients,
                                                            false
                                                        )
                                                    )
                                                } else {
                                                    val totalCal = allFoodData.sumOf { it.calories }
                                                    val totalProtein =
                                                        allFoodData.sumOf { it.protein }
                                                    val totalCarbs = allFoodData.sumOf { it.carbs }
                                                    val totalFat = allFoodData.sumOf { it.fat }
                                                    val formatted = formatMealResponse(
                                                        allFoodData,
                                                        totalCal,
                                                        totalProtein,
                                                        totalCarbs,
                                                        totalFat
                                                    )
                                                    updateMessage(
                                                        messages, loadingIndex,
                                                        ChatMessage(
                                                            formatted, false,
                                                            sourceUrl = allFoodData.firstOrNull()?.sourceUrl,
                                                            sourceLabel = "Meal sources"
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        NutritionIntent.GENERAL_NUTRITION -> {
                                            val response = generateWithLLM(engine, prompt)
                                            updateMessage(
                                                messages,
                                                loadingIndex,
                                                ChatMessage(response, false)
                                            )
                                        }

                                        NutritionIntent.NONE -> {
                                            updateMessage(
                                                messages, loadingIndex,
                                                ChatMessage(
                                                    Vocabulary.get().chatNutritionAssistantFallback,
                                                    false
                                                )
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    updateMessage(
                                        messages, loadingIndex,
                                        ChatMessage(
                                            String.format(
                                                Vocabulary.get().chatErrorTemplate,
                                                e.message
                                            ),
                                            false
                                        )
                                    )
                                } finally {
                                    isGenerating = false
                                }
                            }
                        }
                    },
                    enabled = userInput.trim().isNotEmpty() && !isGenerating,
                    modifier = Modifier.size(48.dp).background(productColor, shape = CircleShape)
                ) {
                    Icon(
                        Icons.Default.Send,
                        Vocabulary.get().send,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (productFormData != null) {
        ProductFormDialog(
            nutritionData = productFormData!!,
            onDismiss = { productFormData = null },
            onSave = { product ->
                scope.launch { productRepository.insertProduct(product) }
                productFormData = null
            }
        )
    }
}

@Composable
private fun ProductFormDialog(
    nutritionData: FoodNutritionData,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(nutritionData.name) }
    var calories by remember { mutableStateOf(nutritionData.calories.toString()) }
    var protein by remember { mutableStateOf(nutritionData.protein.toString()) }
    var carbs by remember { mutableStateOf(nutritionData.carbs.toString()) }
    var fats by remember { mutableStateOf(nutritionData.fat.toString()) }
    var code by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight().clickable { },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(30, 30, 30))
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
                            Vocabulary.get().addToMyProducts,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                Vocabulary.get().close,
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        Vocabulary.get().valuesPer100g,
                        color = Color(0xFFFFEB3B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Vocabulary.get().productName, color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = productColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = productColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text(Vocabulary.get().caloriesPer100g, color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = productColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = productColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text(Vocabulary.get().proteinPer100g, color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = productColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = productColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text(Vocabulary.get().carbsPer100g, color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = productColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = productColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = it },
                        label = { Text(Vocabulary.get().fatsPer100g, color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = productColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = productColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text(Vocabulary.get().barcodeOptional, color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = productColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = productColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val product = Product(
                                name = name,
                                calories = calories.toFloatSafe(),
                                protein = protein.toFloatSafe(),
                                carbs = carbs.toFloatSafe(),
                                fats = fats.toFloatSafe(),
                                code = code
                            )
                            onSave(product)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = productColor)
                    ) {
                        Text(Vocabulary.get().saveProduct, color = Color.White)
                    }
                }
            }
        }
    }
}

private suspend fun generateWithLLM(engine: InferenceEngine, prompt: String): String {
    Log.d("ChatLLM", "=== Sending prompt ===")
    Log.d("ChatLLM", prompt)

    val flow = engine.sendUserPrompt(prompt)
    val allTokens = mutableListOf<String>()
    val thinkTokens = mutableListOf<String>()
    var insideThink = false

    flow.catch { e ->
        Log.e("ChatLLM", "Error during generation: ${e.message}", e)
        throw e
    }.collectLatest { token ->
        val t = token
        Log.d("ChatLLM", "Raw token: '$t'")

        if (t.isBlank()) {
            allTokens.add(" ")
            if (insideThink) thinkTokens.add(" ")
        } else {
            val trimmed = t.trim()
            if (trimmed.startsWith("<think>")) {
                insideThink = true
                thinkTokens.add(trimmed.removePrefix("<think>"))
            } else if (trimmed.endsWith("</think>")) {
                thinkTokens.add(trimmed.removeSuffix("</think>"))
                insideThink = false
            } else if (insideThink) {
                thinkTokens.add(trimmed)
            } else {
                if (!trimmed.startsWith("<")) {
                    allTokens.add(trimmed)
                }
            }
        }
    }

    var response = allTokens.joinToString("")

    if (response.isBlank() && thinkTokens.isNotEmpty()) {
        val thinkText = thinkTokens.joinToString("")
        Log.d("ChatLLM", "=== Model stuck in think block. Extracting last draft ===")
        val draftPattern = Regex(
            """(Chicken breast, raw has \d+\.\d+ calories per \d+g, \d+\.\d+g protein, \d+\.\d+g carbs, and \d+\.\d+g fat.*?USDA FoodData Central\.)""",
            RegexOption.DOT_MATCHES_ALL
        )
        val match = draftPattern.findAll(thinkText).lastOrNull()
        response = match?.value ?: run {
            val lines = thinkText.split(".")
            val lastGood = lines.findLast { it.contains("calories") && it.contains("USDA") }
            lastGood?.trim() + "." ?: Vocabulary.get().failedToGenerateResponse
        }
    }

    response = response.replace(Regex("\\*\\*"), "").replace("*", "").replace("__", "")
        .replace(Regex("\n{3,}"), "\n").trim()
    if (response.isBlank() || response == Vocabulary.get().failedToGenerateResponse) {
        response = Vocabulary.get().defaultFallbackResponse
    }

    Log.d("ChatLLM", "=== Final cleaned response ===")
    Log.d("ChatLLM", response)
    return response
}

private fun updateMessage(
    messages: SnapshotStateList<ChatMessage>,
    index: Int,
    newMessage: ChatMessage
) {
    if (index in messages.indices) {
        messages[index] = newMessage
    } else {
        messages.add(newMessage)
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onAddToProducts: (FoodNutritionData) -> Unit
) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) productColor.copy(alpha = 0.8f) else ContainerBackground
    val shape = if (message.isUser) RoundedCornerShape(
        16.dp,
        4.dp,
        16.dp,
        16.dp
    ) else RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bgColor, shape = shape)
                .padding(12.dp)
        ) {
            if (message.isLoading) {
                CircularProgressIndicator(color = productColor, modifier = Modifier.size(24.dp))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )

                    if (message.sourceUrl != null || message.nutritionData != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (message.sourceUrl != null) {
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(message.sourceUrl)
                                            )
                                        )
                                    },
                                    color = productColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.OpenInNew,
                                            contentDescription = Vocabulary.get().openSource,
                                            tint = productColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = message.sourceLabel ?: Vocabulary.get().source,
                                            color = productColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            if (message.nutritionData != null) {
                                IconButton(
                                    onClick = { onAddToProducts(message.nutritionData) },
                                    modifier = Modifier.size(32.dp)
                                        .background(productColor.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        Vocabulary.get().addToProducts,
                                        tint = productColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (message.text.isNotEmpty() && !message.isLoading) {
            Box(
                modifier = Modifier.padding(top = 2.dp)
                    .align(if (message.isUser) Alignment.End else Alignment.Start)
            ) {
                IconButton(
                    onClick = { clipboardManager.setText(AnnotatedString(message.text)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        Vocabulary.get().copyMessage,
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}