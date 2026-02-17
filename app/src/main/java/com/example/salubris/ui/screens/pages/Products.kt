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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salubris.database.AppDatabase
import com.example.salubris.ui.components.Input
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.productColor
import com.example.salubris.utils.FieldType
import com.example.salubris.utils.FormData
import com.example.salubris.utils.RenderFormFields
import kotlinx.coroutines.launch

data class ProductData(
    val name: String,
    val kcal: Number,
    val protein: Number,
    val carbs: Number,
    val fats: Number
)


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
    val productDao = AppDatabase.getDatabase(context).productDao()

    // Collect products from Flow for automatic UI updates
    val products by productDao.getProducts().collectAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize()) {

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Add Product Button
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

            // Product list
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
                    for (product in products) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(5.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text("${product.calories} kcal")
                        }
                    }
                }
            }
        }

        // Modal for adding product
        Modal(
            open = isOpen,
            onClose = { isOpen = false },
            title = "Add a product",
            onSubmit = {
                // Extract form values
                val name = fields[0].value as String
                val calories = (fields[1].value as Number).toInt()
                val protein = (fields[2].value as Number).toFloat()
                val carbs = (fields[3].value as Number).toFloat()
                val fats = (fields[4].value as Number).toFloat()

                // Insert product using coroutine (non-blocking)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val newProduct = com.example.salubris.database.entities.Product(
                        name = name,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fats = fats
                    )
                    productDao.insert(newProduct)
                }

                isOpen = false

                // Clear form safely
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


