package com.example.salubris.ui.screens.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.Product
import com.example.salubris.database.repositories.ProductRepository
import com.example.salubris.ui.theme.*
import com.example.salubris.utils.toFloatSafe
import kotlinx.coroutines.launch
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.net.ConnectivityManager
import android.content.Context
import android.net.NetworkCapabilities
import android.os.Build

data class FetchedProduct(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val code: String
)

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
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

    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateProduct by remember { mutableStateOf<Product?>(null) }

    var isFormOpen by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var prefilledData by remember { mutableStateOf<FetchedProduct?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val client = HttpClient(CIO)

    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = remember { ProductRepository(database.productDao()) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val products by repository.getAllProducts().collectAsState(initial = emptyList())

    // Handle barcode scan
    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode != null && isScanning) {
            val existing = products.firstOrNull { it.code == scannedBarcode }
            if (existing != null) {
                duplicateProduct = existing
                showDuplicateDialog = true
                isScanning = false
                scannedBarcode = null
                return@LaunchedEffect
            }

            if (!isInternetAvailable(context)) {
                showNoInternetSnackbar = true
                isScanning = false
                scannedBarcode = null
                return@LaunchedEffect
            }

            isLookingUpProduct = true
            scope.launch {
                try {
                    val jsonString = client.get("https://world.openfoodfacts.org/api/v2/product/$scannedBarcode").bodyAsText()
                    val response = Json { ignoreUnknownKeys = true }.decodeFromString<OpenFoodFactsResponse>(jsonString)
                    val productData = response.product
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
                } catch (e: Exception) {
                    Log.e("Products", "Error fetching product: ${e.message}")
                    showNoInternetSnackbar = true
                    isLookingUpProduct = false
                    isScanning = false
                    scannedBarcode = null
                } finally {
                    client.close()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(5.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 10.dp),
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
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Product", color = Color.White)
                }

                Button(
                    onClick = {
                        if (isInternetAvailable(context)) isScanning = true
                        else showNoInternetSnackbar = true
                    },
                    colors = ButtonDefaults.buttonColors(productColor.copy(alpha = 0.8f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Scan Barcode", color = Color.White)
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
                    Text("No products yet", color = Color.Gray)
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
                                    Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(modifier = Modifier.background(caloriesColor, shape = RoundedCornerShape(5.dp)).padding(5.dp)) {
                                            Text("K:", color = Color.White); Text("${product.calories}", color = Color.White)
                                        }
                                        Row(modifier = Modifier.background(proteinColor, shape = RoundedCornerShape(5.dp)).padding(5.dp)) {
                                            Text("P:", color = Color.White); Text("${product.protein}", color = Color.White)
                                        }
                                        Row(modifier = Modifier.background(carbsColor, shape = RoundedCornerShape(5.dp)).padding(5.dp)) {
                                            Text("C:", color = Color.White); Text("${product.carbs}", color = Color.White)
                                        }
                                        Row(modifier = Modifier.background(fatsColor, shape = RoundedCornerShape(5.dp)).padding(5.dp)) {
                                            Text("F:", color = Color.White); Text("${product.fats}", color = Color.White)
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { scope.launch { repository.deleteProduct(product) } },
                                    modifier = Modifier.size(40.dp)
                                        .background(cancelColor.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Delete, "Delete", tint = cancelColor, modifier = Modifier.size(30.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showNoInternetSnackbar) {
            Snackbar(modifier = Modifier.padding(16.dp), containerColor = cancelColor, contentColor = Color.White,
                action = { TextButton(onClick = { showNoInternetSnackbar = false }) { Text("Dismiss", color = Color.White) } }
            ) { Text("No internet connection") }
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(3000); showNoInternetSnackbar = false }
        }
        if (showDuplicateSnackbar) {
            Snackbar(modifier = Modifier.padding(16.dp), containerColor = Color.Yellow, contentColor = Color.Black,
                action = { TextButton(onClick = { showDuplicateSnackbar = false }) { Text("Dismiss", color = Color.Black) } }
            ) { Text("Product already exists") }
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(3000); showDuplicateSnackbar = false }
        }
        if (showNotFoundSnackbar) {
            Snackbar(modifier = Modifier.padding(16.dp), containerColor = Color.Gray, contentColor = Color.White,
                action = { TextButton(onClick = { showNotFoundSnackbar = false }) { Text("Dismiss", color = Color.White) } }
            ) { Text("Product not found in database") }
            LaunchedEffect(Unit) { kotlinx.coroutines.delay(3000); showNotFoundSnackbar = false }
        }

        // Duplicate product dialog
        if (showDuplicateDialog && duplicateProduct != null) {
            Dialog(onDismissRequest = { showDuplicateDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable { showDuplicateDialog = false }, contentAlignment = Alignment.Center) {
                    Card(modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight().clickable { }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(30, 30, 30))) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Product Already Exists", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            val p = duplicateProduct!!
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Name: ${p.name}", color = Color.White)
                                Text("Calories: ${p.calories}", color = Color.White)
                                Text("Protein: ${p.protein}", color = Color.White)
                                Text("Carbs: ${p.carbs}", color = Color.White)
                                Text("Fats: ${p.fats}", color = Color.White)
                                Text("Barcode: ${p.code}", color = Color.White)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { showDuplicateDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancel", color = Color.White) }
                                Button(onClick = {
                                    editingProduct = p
                                    prefilledData = null
                                    isFormOpen = true
                                    showDuplicateDialog = false
                                }, colors = ButtonDefaults.buttonColors(containerColor = productColor)) { Text("Update", color = Color.White) }
                            }
                        }
                    }
                }
            }
        }

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
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight()
                            .clickable { },
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
                                    if (editingProduct != null) "Edit Product" else "Add a product",
                                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { isFormOpen = false }) {
                                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                                }
                            }

                            // Data source note
                            if (prefilledData != null) {
                                Column(
                                    modifier = Modifier
                                        .background(Color(80, 80, 80), shape = RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Data sourced from Open Food Facts. It may be outdated or incomplete.", color = Color.White, fontSize = 12.sp)
                                    Text(
                                        "Visit Open Food Facts",
                                        color = Color(0xFF4DB8FF),
                                        textDecoration = TextDecoration.Underline,
                                        fontSize = 12.sp,
                                        modifier = Modifier.clickable {
                                            uriHandler.openUri("https://world.openfoodfacts.org/product/${prefilledData!!.code}")
                                        }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Name", color = Color.White) },
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
                                label = { Text("Calories", color = Color.White) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = productColor,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = productColor
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = protein,
                                onValueChange = { protein = it },
                                label = { Text("Protein", color = Color.White) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = productColor,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = productColor
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = carbs,
                                onValueChange = { carbs = it },
                                label = { Text("Carbs", color = Color.White) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = productColor,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = productColor
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = fats,
                                onValueChange = { fats = it },
                                label = { Text("Fats", color = Color.White) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = productColor,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = productColor
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it },
                                label = { Text("Barcode", color = Color.White) },
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
                                Text(if (editingProduct != null) "Update Product" else "Save Product", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Barcode scanner modal
        if (isScanning) {
            Dialog(onDismissRequest = { isScanning = false; scannedBarcode = null }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLookingUpProduct) {
                        Column(modifier = Modifier.fillMaxSize().background(Color.Black), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Looking up product...", color = Color.White)
                            Button(onClick = { isLookingUpProduct = false; isScanning = false }, modifier = Modifier.padding(top = 50.dp)) { Text("Cancel") }
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

@Serializable data class OpenFoodFactsResponse(val product: ProductData? = null)
@Serializable data class ProductData(@SerialName("product_name") val name: String? = "", val nutriments: Nutriments? = null)
@Serializable data class Nutriments(
    @SerialName("energy-kcal") val energyKcal: Double? = 0.0,
    val proteins: Double? = 0.0,
    val carbohydrates: Double? = 0.0,
    val fat: Double? = 0.0
)