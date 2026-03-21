package com.example.salubris.ui.screens.pages

import Modal
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.Product
import com.example.salubris.database.repositories.ProductRepository
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.cancelColor
import com.example.salubris.ui.theme.productColor
import com.example.salubris.utils.FieldType
import com.example.salubris.utils.FormData
import com.example.salubris.utils.RenderFormFields
import kotlinx.coroutines.launch

@Composable
fun Products() {
    var isOpen by remember { mutableStateOf(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row {
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
                                    .background(Color(100,100,100), RoundedCornerShape(5.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column() {
                                    Text(product.name, fontWeight = FontWeight.Bold)
                                    Row() {
                                        Text("${product.calories} kcal")
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

        Modal(
            open = isOpen,
            onClose = { isOpen = false },
            title = "Add a product",
            onSubmit = {
                val name = fields[0].value as String
                val calories = (fields[1].value as Number).toInt()
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
    }
}


