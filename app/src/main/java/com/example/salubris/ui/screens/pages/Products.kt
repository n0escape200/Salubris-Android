package com.example.salubris.ui.screens.pages

import android.util.Log
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.Product
import com.example.salubris.database.repositories.ProductRepository
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.ui.theme.cancelColor
import com.example.salubris.ui.theme.carbsColor
import com.example.salubris.ui.theme.fatsColor
import com.example.salubris.ui.theme.productColor
import com.example.salubris.ui.theme.proteinColor
import com.example.salubris.utils.Vocabulary
import com.example.salubris.utils.toFloatSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

data class FetchedProduct(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val code: String
)

// ============================================================
// RELIABLE INTERNET CHECK (suspend, performs actual HTTP request)
// ============================================================
suspend fun isInternetAvailable(): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.google.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.requestMethod = "HEAD"
            val responseCode = connection.responseCode
            responseCode in 200..299
        } catch (_: Exception) {
            false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Products() {
    var isScanning by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var isLookingUpProduct by remember { mutableStateOf(false) }
    var showNoInternetSnackbar by remember { mutableStateOf(false) }
    var showDuplicateSnackbar by remember { mutableStateOf(false) }
    var showNotFoundSnackbar by remember { mutableStateOf(false) }
    var showApiErrorSnackbar by remember { mutableStateOf(false) }

    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateProduct by remember { mutableStateOf<Product?>(null) }

    var isFormOpen by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var prefilledData by remember { mutableStateOf<FetchedProduct?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val client = remember {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = remember { ProductRepository(database.productDao()) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val products by repository.getAllProducts().collectAsState(initial = emptyList())

    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode != null && isScanning) {
            // 1. Validate barcode
            if (scannedBarcode.isNullOrBlank()) {
                isScanning = false
                scannedBarcode = null
                return@LaunchedEffect
            }

            // 2. Duplicate check
            val existing = products.firstOrNull { it.code == scannedBarcode }
            if (existing != null) {
                duplicateProduct = existing
                showDuplicateDialog = true
                isScanning = false
                scannedBarcode = null
                return@LaunchedEffect
            }

            // 3. Check internet
            val hasInternet = isInternetAvailable()
            if (!hasInternet) {
                showNoInternetSnackbar = true
                isScanning = false
                scannedBarcode = null
                return@LaunchedEffect
            }

            // 4. Start lookup
            isLookingUpProduct = true
            scope.launch {
                try {
                    // Force network operations to IO dispatcher
                    val jsonString = withContext(Dispatchers.IO) {
                        val request = Request.Builder()
                            .url("https://world.openfoodfacts.org/api/v2/product/$scannedBarcode")
                            .build()
                        val response = client.newCall(request).execute()
                        if (!response.isSuccessful) {
                            Log.e("Products", "HTTP error: ${response.code} - ${response.message}")
                            throw Exception("HTTP error ${response.code}: ${response.message}")
                        }
                        response.body?.string() ?: ""
                    }

                    if (jsonString.isBlank()) {
                        throw Exception("Empty response body")
                    }
                    Log.d("Products", "API response: $jsonString")

                    val parsedResponse = Json { ignoreUnknownKeys = true }
                        .decodeFromString<OpenFoodFactsResponse>(jsonString)

                    // Check the status field: 0 = product not found, 1 = found
                    if (parsedResponse.status == 0) {
                        showNotFoundSnackbar = true
                        isLookingUpProduct = false
                        isScanning = false
                        scannedBarcode = null
                        return@launch
                    }

                    val productData = parsedResponse.product
                    val name = productData?.name ?: ""
                    if (name.isBlank() && productData?.nutriments == null) {
                        showNotFoundSnackbar = true
                        isLookingUpProduct = false
                        isScanning = false
                        scannedBarcode = null
                        return@launch
                    }

                    val fetched = FetchedProduct(
                        name = name,
                        calories = productData?.nutriments?.energyKcal ?: 0.0,
                        protein = productData?.nutriments?.proteins ?: 0.0,
                        carbs = productData?.nutriments?.carbohydrates ?: 0.0,
                        fats = productData?.nutriments?.fat ?: 0.0,
                        code = scannedBarcode!!
                    )
                    prefilledData = fetched
                    editingProduct = null
                    isFormOpen = true
                    isLookingUpProduct = false
                    isScanning = false
                    scannedBarcode = null
                } catch (e: UnknownHostException) {
                    Log.e("Products", "UnknownHostException: ${e.message}")
                    showNoInternetSnackbar = true
                    isLookingUpProduct = false
                    isScanning = false
                    scannedBarcode = null
                } catch (e: SocketTimeoutException) {
                    Log.e("Products", "SocketTimeoutException: ${e.message}")
                    showNoInternetSnackbar = true
                    isLookingUpProduct = false
                    isScanning = false
                    scannedBarcode = null
                } catch (e: IOException) {
                    Log.e("Products", "IOException: ${e.message}", e)
                    showNoInternetSnackbar = true
                    isLookingUpProduct = false
                    isScanning = false
                    scannedBarcode = null
                } catch (e: SSLException) {
                    Log.e("Products", "SSLException: ${e.message}", e)
                    showNoInternetSnackbar = true
                    isLookingUpProduct = false
                    isScanning = false
                    scannedBarcode = null
                } catch (e: Exception) {
                    Log.e("Products", "Unexpected error: ${e.message}", e)
                    showApiErrorSnackbar = true
                    isLookingUpProduct = false
                    isScanning = false
                    scannedBarcode = null
                }
            }
        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        editingProduct = null
                        prefilledData = null
                        isFormOpen = true
                    },
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
                    Text(Vocabulary.get().addProduct, color = Color.White)
                }

                Button(
                    onClick = {
                        scope.launch {
                            if (isInternetAvailable()) isScanning = true
                            else showNoInternetSnackbar = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(productColor.copy(alpha = 0.8f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(Vocabulary.get().scanBarcode, color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .background(ContainerBackground, RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (products.isEmpty()) {
                    Text(Vocabulary.get().noProductsYet, color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(products) { _, product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(60, 60, 60), RoundedCornerShape(5.dp))
                                    .clickable {
                                        editingProduct = product
                                        prefilledData = null
                                        isFormOpen = true
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        product.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .background(
                                                    caloriesColor,
                                                    shape = RoundedCornerShape(5.dp)
                                                )
                                                .padding(5.dp)
                                        ) {
                                            Text(
                                                "K:",
                                                color = Color.White
                                            ); Text("${product.calories}", color = Color.White)
                                        }
                                        Row(
                                            modifier = Modifier
                                                .background(
                                                    proteinColor,
                                                    shape = RoundedCornerShape(5.dp)
                                                )
                                                .padding(5.dp)
                                        ) {
                                            Text(
                                                "P:",
                                                color = Color.White
                                            ); Text("${product.protein}", color = Color.White)
                                        }
                                        Row(
                                            modifier = Modifier
                                                .background(
                                                    carbsColor,
                                                    shape = RoundedCornerShape(5.dp)
                                                )
                                                .padding(5.dp)
                                        ) {
                                            Text(
                                                "C:",
                                                color = Color.White
                                            ); Text("${product.carbs}", color = Color.White)
                                        }
                                        Row(
                                            modifier = Modifier
                                                .background(
                                                    fatsColor,
                                                    shape = RoundedCornerShape(5.dp)
                                                )
                                                .padding(5.dp)
                                        ) {
                                            Text("F:", color = Color.White); Text(
                                            "${product.fats}",
                                            color = Color.White
                                        )
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { scope.launch { repository.deleteProduct(product) } },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            cancelColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        Vocabulary.get().delete,
                                        tint = cancelColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // Snackbars
        // ============================================================
        if (showNoInternetSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = cancelColor,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { showNoInternetSnackbar = false }) {
                        Text(Vocabulary.get().dismiss, color = Color.White)
                    }
                }
            ) { Text(Vocabulary.get().noInternetConnection) }
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(3000); showNoInternetSnackbar = false }
        }
        if (showDuplicateSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color.Yellow,
                contentColor = Color.Black,
                action = {
                    TextButton(onClick = { showDuplicateSnackbar = false }) {
                        Text(Vocabulary.get().dismiss, color = Color.Black)
                    }
                }
            ) { Text(Vocabulary.get().productAlreadyExistsSnack) }
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(3000); showDuplicateSnackbar = false }
        }
        if (showNotFoundSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color.Gray,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { showNotFoundSnackbar = false }) {
                        Text(Vocabulary.get().dismiss, color = Color.White)
                    }
                }
            ) { Text(Vocabulary.get().productNotFoundSnack) }
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(3000); showNotFoundSnackbar = false }
        }
        if (showApiErrorSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color.Red,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { showApiErrorSnackbar = false }) {
                        Text(Vocabulary.get().dismiss, color = Color.White)
                    }
                }
            ) { Text(Vocabulary.get().apiErrorSnack) }
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(3000); showApiErrorSnackbar = false }
        }

        // ============================================================
        // Duplicate product dialog
        // ============================================================
        if (showDuplicateDialog && duplicateProduct != null) {
            Dialog(
                onDismissRequest = { showDuplicateDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { showDuplicateDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight()
                            .clickable { },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(30, 30, 30))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                Vocabulary.get().productAlreadyExists,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val p = duplicateProduct!!
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "${Vocabulary.get().productName}: ${p.name}",
                                    color = Color.White
                                )
                                Text(
                                    "${Vocabulary.get().calories}: ${p.calories}",
                                    color = Color.White
                                )
                                Text(
                                    "${Vocabulary.get().protein}: ${p.protein}",
                                    color = Color.White
                                )
                                Text("${Vocabulary.get().carbs}: ${p.carbs}", color = Color.White)
                                Text("${Vocabulary.get().fats}: ${p.fats}", color = Color.White)
                                Text(
                                    "${Vocabulary.get().barcodeOptional}: ${p.code}",
                                    color = Color.White
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { showDuplicateDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                ) { Text(Vocabulary.get().cancel, color = Color.White) }
                                Button(
                                    onClick = {
                                        editingProduct = p
                                        prefilledData = null
                                        isFormOpen = true
                                        showDuplicateDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = productColor)
                                ) { Text(Vocabulary.get().update, color = Color.White) }
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // Add/Edit product form – redesigned for clarity and equal sizing
        // ============================================================
        if (isFormOpen) {
            var name by remember { mutableStateOf("") }
            var calories by remember { mutableStateOf("") }
            var protein by remember { mutableStateOf("") }
            var carbs by remember { mutableStateOf("") }
            var fats by remember { mutableStateOf("") }
            var code by remember { mutableStateOf("") }

            LaunchedEffect(editingProduct, prefilledData) {
                when {
                    editingProduct != null -> {
                        val p = editingProduct!!
                        name = p.name
                        calories = "%.2f".format(p.calories)
                        protein = "%.2f".format(p.protein)
                        carbs = "%.2f".format(p.carbs)
                        fats = "%.2f".format(p.fats)
                        code = p.code
                    }

                    prefilledData != null -> {
                        val f = prefilledData!!
                        name = f.name
                        calories = "%.2f".format(f.calories)
                        protein = "%.2f".format(f.protein)
                        carbs = "%.2f".format(f.carbs)
                        fats = "%.2f".format(f.fats)
                        code = f.code
                    }

                    else -> {
                        name = ""; calories = ""; protein = ""; carbs = ""; fats = ""; code = ""
                    }
                }
            }

            Dialog(
                onDismissRequest = { isFormOpen = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { isFormOpen = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .wrapContentHeight()
                            .clickable { },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(30, 30, 30)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (editingProduct != null) Vocabulary.get().editProductTitle else Vocabulary.get().addProductTitle,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { isFormOpen = false }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = Vocabulary.get().close,
                                        tint = Color.White
                                    )
                                }
                            }

                            // Data source note (if prefilled)
                            if (prefilledData != null) {
                                Column(
                                    modifier = Modifier
                                        .background(Color(80, 80, 80), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        Vocabulary.get().dataFromOpenFoodFacts,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        Vocabulary.get().visitOpenFoodFacts,
                                        color = Color(0xFF4DB8FF),
                                        textDecoration = TextDecoration.Underline,
                                        fontSize = 12.sp,
                                        modifier = Modifier.clickable {
                                            uriHandler.openUri("https://world.openfoodfacts.org/product/${prefilledData!!.code}")
                                        }
                                    )
                                }
                            }

                            // Product name
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

                            // "per 100g" note
                            Text(
                                Vocabulary.get().nutritionalValuesPer100g,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            // Macro fields – 2 columns with equal weight
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = calories,
                                        onValueChange = { calories = it },
                                        label = {
                                            Text(
                                                Vocabulary.get().calories,
                                                color = Color.White
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = productColor,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = productColor
                                        ),
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        trailingIcon = {
                                            Text(
                                                "kcal",
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    )
                                    OutlinedTextField(
                                        value = protein,
                                        onValueChange = { protein = it },
                                        label = {
                                            Text(
                                                Vocabulary.get().protein,
                                                color = Color.White
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = productColor,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = productColor
                                        ),
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        trailingIcon = {
                                            Text(
                                                "g",
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = carbs,
                                        onValueChange = { carbs = it },
                                        label = {
                                            Text(
                                                Vocabulary.get().carbs,
                                                color = Color.White
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = productColor,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = productColor
                                        ),
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        trailingIcon = {
                                            Text(
                                                "g",
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    )
                                    OutlinedTextField(
                                        value = fats,
                                        onValueChange = { fats = it },
                                        label = {
                                            Text(
                                                Vocabulary.get().fats,
                                                color = Color.White
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = productColor,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = productColor
                                        ),
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        trailingIcon = {
                                            Text(
                                                "g",
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    )
                                }
                            }

                            // Barcode (optional)
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it },
                                label = {
                                    Text(
                                        Vocabulary.get().barcodeOptional,
                                        color = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = productColor,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = productColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Save button
                            Button(
                                onClick = {
                                    scope.launch {
                                        if (editingProduct != null) {
                                            repository.updateProduct(
                                                editingProduct!!.copy(
                                                    name = name,
                                                    calories = calories.toFloatSafe(),
                                                    protein = protein.toFloatSafe(),
                                                    carbs = carbs.toFloatSafe(),
                                                    fats = fats.toFloatSafe(),
                                                    code = code
                                                )
                                            )
                                        } else {
                                            repository.insertProduct(
                                                Product(
                                                    name = name,
                                                    calories = calories.toFloatSafe(),
                                                    protein = protein.toFloatSafe(),
                                                    carbs = carbs.toFloatSafe(),
                                                    fats = fats.toFloatSafe(),
                                                    code = code
                                                )
                                            )
                                        }
                                    }
                                    isFormOpen = false
                                    editingProduct = null
                                    prefilledData = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = productColor)
                            ) {
                                Text(
                                    if (editingProduct != null) Vocabulary.get().updateProduct else Vocabulary.get().saveProduct,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // Barcode scanner modal
        // ============================================================
        if (isScanning) {
            Dialog(
                onDismissRequest = { isScanning = false; scannedBarcode = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLookingUpProduct) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(Vocabulary.get().lookingUpProduct, color = Color.White)
                            Button(
                                onClick = { isLookingUpProduct = false; isScanning = false },
                                modifier = Modifier.padding(top = 50.dp)
                            ) { Text(Vocabulary.get().cancel) }
                        }
                    } else {
                        CameraScreen(
                            onBarcodeScanned = { barcode -> scannedBarcode = barcode },
                            onClose = { isScanning = false; scannedBarcode = null },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Open Food Facts response models
// ============================================================
@Serializable
data class OpenFoodFactsResponse(
    val status: Int = 1,          // 0 = not found, 1 = found
    val product: ProductData? = null
)

@Serializable
data class ProductData(
    @SerialName("product_name") val name: String? = "",
    val nutriments: Nutriments? = null
)

@Serializable
data class Nutriments(
    @SerialName("energy-kcal") val energyKcal: Double? = 0.0,
    val proteins: Double? = 0.0,
    val carbohydrates: Double? = 0.0,
    val fat: Double? = 0.0
)