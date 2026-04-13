package com.example.salubris.ui.screens.pages

import Modal
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

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

@Composable
fun Products() {
    var isOpen by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var isLookingUpProduct by remember { mutableStateOf(false) }
    var showNoInternetSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val client = HttpClient(CIO)
    val fields = remember {
        mutableStateListOf(
            FormData("Name", FieldType.STRING, ""),
            FormData("Calories", FieldType.NUMBER, 0),
            FormData("Protein", FieldType.NUMBER, 0),
            FormData("Carbs", FieldType.NUMBER, 0),
            FormData("Fats", FieldType.NUMBER, 0)
        )
    }

    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = remember { ProductRepository(database.productDao()) }

    val scope = rememberCoroutineScope()

    // Collect products through repository
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
            // Check internet connectivity before attempting to lookup product
            if (!isInternetAvailable(context)) {
                showNoInternetSnackbar = true
                isScanning = false
                isLookingUpProduct = false
                scannedBarcode = null
                return@LaunchedEffect
            }

            isLookingUpProduct = true

            // Try to lookup product from barcode
            scope.launch {
                try {
                    val jsonString = client.get("https://world.openfoodfacts.org/api/v2/product/$scannedBarcode").bodyAsText()
                    val response = Json { ignoreUnknownKeys = true }.decodeFromString<OpenFoodFactsResponse>(jsonString)
                    Log.v("TAG", response.toString())

                    fields[0].value = response.product?.name ?: ""
                    fields[1].value = response.product?.nutriments?.energyKcal ?: 0.0
                    fields[2].value = response.product?.nutriments?.proteins ?: 0.0
                    fields[3].value = response.product?.nutriments?.carbohydrates ?: 0.0
                    fields[4].value = response.product?.nutriments?.fat ?: 0.0

                    isOpen = true
                    isLookingUpProduct = false
                } catch (e: Exception) {
                    // Handle network errors
                    Log.e("TAG", "Error fetching product: ${e.message}")
                    showNoInternetSnackbar = true
                    isLookingUpProduct = false
                } finally {
                    isScanning = false
                    scannedBarcode = null
                    client.close()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

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
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Add Product", color = Color.White)
                }

                Button(
                    onClick = {
                        // Check internet connectivity before opening scanner
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
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
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
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(products) { index, product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(60, 60, 60), RoundedCornerShape(5.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                                    Row( horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.background(caloriesColor, shape = RoundedCornerShape(5.dp))
                                                .padding(5.dp)
                                        ){
                                            Text("K:", color = Color.White)
                                            Text("${product.calories}", color = Color.White)
                                        }
                                        Row(
                                            modifier = Modifier.background(proteinColor, shape = RoundedCornerShape(5.dp))
                                                .padding(5.dp)
                                        ){
                                            Text("P:", color = Color.White)
                                            Text("${product.protein}", color = Color.White)
                                        }
                                        Row(
                                            modifier = Modifier.background(carbsColor, shape = RoundedCornerShape(5.dp))
                                                .padding(5.dp)
                                        ){
                                            Text("C:", color = Color.White)
                                            Text("${product.carbs}", color = Color.White)
                                        }
                                        Row(
                                            modifier = Modifier.background(fatsColor, shape = RoundedCornerShape(5.dp))
                                                .padding(5.dp)
                                        ){
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
                                        .background(
                                            color = cancelColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete product",
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

        // Snackbar for no internet feedback
        if (showNoInternetSnackbar) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
            LaunchedEffect(showNoInternetSnackbar) {
                snackbarHostState.showSnackbar(
                    message = "No internet connection. Please check your network and try again."
                )
                showNoInternetSnackbar = false
            }
        }

        Modal(
            open = isOpen,
            onClose = {
                isOpen = false
                isScanning = false
            },
            title = "Add a product",
            onSubmit = {
                val name = fields[0].value as String
                val calories = (fields[1].value as Number).toFloat()
                val protein = (fields[2].value as Number).toFloat()
                val carbs = (fields[3].value as Number).toFloat()
                val fats = (fields[4].value as Number).toFloat()

                scope.launch {
                    val newProduct = Product(
                        name = name,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fats = fats
                    )
                    repository.insertProduct(newProduct)
                }

                isOpen = false
                isScanning = false

                fields.forEachIndexed { index, form ->
                    fields[index] = when (form.type) {
                        FieldType.STRING -> form.copy(value = "")
                        FieldType.NUMBER -> form.copy(value = 0)
                        else -> form
                    }
                }
            }
        ) {
            RenderFormFields(fields)
        }

        // Barcode Scanner Modal
        if (isScanning) {
            Dialog(
                onDismissRequest = {
                    isScanning = false
                    scannedBarcode = null
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLookingUpProduct) {
                        // Show loading state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Looking up product...", color = Color.White)
                            Button(
                                onClick = {
                                    isLookingUpProduct = false
                                    isScanning = false
                                },
                                modifier = Modifier.padding(top = 32.dp)
                            ) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        // Camera scanner - full screen
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