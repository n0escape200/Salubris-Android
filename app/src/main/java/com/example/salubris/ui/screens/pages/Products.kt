package com.example.salubris.ui.screens.pages

import Modal
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable          // added
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close       // added
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip               // added
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.Product
import com.example.salubris.database.repositories.ProductRepository
import com.example.salubris.ui.theme.*
import com.example.salubris.utils.FieldType
import com.example.salubris.utils.FormData
import com.example.salubris.utils.RenderFormFields
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
import androidx.compose.foundation.layout.wrapContentHeight
import com.example.salubris.utils.toFloatSafe

// ----------------------------------------------------------------------
// Helper definitions moved before Products composable
// ----------------------------------------------------------------------

// Helper function to check internet connectivity
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

// Helper data class for fetched product info
data class FetchedProduct(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val code: String
)

// ----------------------------------------------------------------------
// Products composable (unchanged except for Dialog replacement)
// ----------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Products() {
    var isOpen by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var isLookingUpProduct by remember { mutableStateOf(false) }
    var showNoInternetSnackbar by remember { mutableStateOf(false) }
    var showDuplicateSnackbar by remember { mutableStateOf(false) }
    var showNotFoundSnackbar by remember { mutableStateOf(false) }
    var showProductConfirmDialog by remember { mutableStateOf(false) }
    var fetchedProductData by remember { mutableStateOf<FetchedProduct?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val client = HttpClient(CIO)
    val fields = remember {
        mutableStateListOf(
            FormData("Name", FieldType.STRING, ""),
            FormData("Calories", FieldType.NUMBER, ""),
            FormData("Protein", FieldType.NUMBER, ""),
            FormData("Carbs", FieldType.NUMBER, ""),
            FormData("Fats", FieldType.NUMBER, ""),
            FormData("Code", FieldType.STRING, "")
        )
    }

    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = remember { ProductRepository(database.productDao()) }

    val scope = rememberCoroutineScope()

    val products by repository
        .getAllProducts()
        .collectAsState(initial = emptyList())
    val mutableProducts = remember { mutableStateListOf<Product>() }

    LaunchedEffect (products) {
        mutableProducts.clear()
        mutableProducts.addAll(products)
    }

    // Handle barcode scan result
    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode != null && isScanning) {
            val alreadyExists = products.any { it.code == scannedBarcode }
            if (alreadyExists) {
                showDuplicateSnackbar = true
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
                    Log.v("TAG", response.toString())

                    val productData = response.product
                    val name = productData?.name ?: ""
                    if (name.isBlank() && productData?.nutriments == null) {
                        showNotFoundSnackbar = true
                        isLookingUpProduct = false
                        isScanning = false
                        scannedBarcode = null
                        return@launch
                    }

                    fetchedProductData = FetchedProduct(
                        name = name,
                        calories = productData?.nutriments?.energyKcal ?: 0.0,
                        protein = productData?.nutriments?.proteins ?: 0.0,
                        carbs = productData?.nutriments?.carbohydrates ?: 0.0,
                        fats = productData?.nutriments?.fat ?: 0.0,
                        code = scannedBarcode!!
                    )
                    showProductConfirmDialog = true
                    isLookingUpProduct = false
                } catch (e: Exception) {
                    Log.e("TAG", "Error fetching product: ${e.message}")
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
                    onClick = { isOpen = true },
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
                        if (isInternetAvailable(context)) {
                            isScanning = true
                        } else {
                            showNoInternetSnackbar = true
                        }
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
                        itemsIndexed(products) { index, product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(60, 60, 60), RoundedCornerShape(5.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(modifier = Modifier.background(caloriesColor, shape = RoundedCornerShape(5.dp)).padding(5.dp)) {
                                            Text("K:", color = Color.White)
                                            Text("${product.calories}", color = Color.White)
                                        }
                                        Row(modifier = Modifier.background(proteinColor, shape = RoundedCornerShape(5.dp)).padding(5.dp)) {
                                            Text("P:", color = Color.White)
                                            Text("${product.protein}", color = Color.White)
                                        }
                                        Row(modifier = Modifier.background(carbsColor, shape = RoundedCornerShape(5.dp)).padding(5.dp)) {
                                            Text("C:", color = Color.White)
                                            Text("${product.carbs}", color = Color.White)
                                        }
                                        Row(modifier = Modifier.background(fatsColor, shape = RoundedCornerShape(5.dp)).padding(5.dp)) {
                                            Text("F:", color = Color.White)
                                            Text("${product.fats}", color = Color.White)
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.deleteProduct(product)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(cancelColor.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete product", tint = cancelColor, modifier = Modifier.size(30.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Snackbars (unchanged, omitted for brevity – same as original)
        // ... (keep all snackbar logic as before)

        // Product add dialog (native Dialog replacing Modal)
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
                                Text("Add a product", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { isOpen = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            }
                            RenderFormFields(fields)
                            Button(
                                onClick = {
                                    val name = fields[0].value as String
                                    val calories = fields[1].value
                                    val protein = fields[2].value
                                    val carbs = fields[3].value
                                    val fats = fields[4].value
                                    val code = fields[5].value as String
                                    scope.launch {
                                        val newProduct = Product(
                                            name = name,
                                            calories = calories.toFloatSafe(),
                                            protein = protein.toFloatSafe(),
                                            carbs = carbs.toFloatSafe(),
                                            fats = fats.toFloatSafe(),
                                            code = code
                                        )
                                        repository.insertProduct(newProduct)
                                    }
                                    isOpen = false
                                    fields.forEachIndexed { index, form ->
                                        fields[index] = when (form.type) {
                                            FieldType.STRING -> form.copy(value = "")
                                            FieldType.NUMBER -> form.copy(value = "")
                                            else -> form
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = productColor)
                            ) {
                                Text("Save Product", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Barcode scanner modal (unchanged)
        if (isScanning) {
            Dialog(
                onDismissRequest = {
                    isScanning = false
                    scannedBarcode = null
                },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLookingUpProduct) {
                        Column(
                            modifier = Modifier.fillMaxSize().background(Color.Black),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Looking up product...", color = Color.White)
                            Button(onClick = {
                                isLookingUpProduct = false
                                isScanning = false
                                scannedBarcode = null
                            }, modifier = Modifier.padding(top = 50.dp)) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        CameraScreen(
                            onBarcodeScanned = { barcode ->
                                scannedBarcode = barcode
                            },
                            onClose = {
                                isScanning = false
                                scannedBarcode = null
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

// Keep the data classes for JSON parsing (unchanged)
@Serializable
data class OpenFoodFactsResponse(
    val product: ProductData? = null
)

@Serializable
data class ProductData(
    @SerialName("product_name")
    val name: String? = "",
    val nutriments: Nutriments? = null
)

@Serializable
data class Nutriments(
    @SerialName("energy-kcal")
    val energyKcal: Double? = 0.0,
    val proteins: Double? = 0.0,
    val carbohydrates: Double? = 0.0,
    val fat: Double? = 0.0
)