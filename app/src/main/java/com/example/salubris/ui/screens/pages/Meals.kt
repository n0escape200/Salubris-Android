package com.example.salubris.ui.screens.pages

import Modal
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.MealWithProducts
import com.example.salubris.database.relations.ProductWithQuantity
import com.example.salubris.database.repositories.MealRepository
import com.example.salubris.database.viewmodels.MealViewModel
import com.example.salubris.database.viewmodels.MealViewModelFactory
import com.example.salubris.database.viewmodels.ProductViewModel
import com.example.salubris.database.viewmodels.productViewModelFactory
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.cancelColor
import com.example.salubris.ui.theme.productColor
import com.example.salubris.utils.ProductNutritionLabel
import kotlinx.coroutines.launch
import com.example.salubris.utils.calculateMacrosForProduct
import com.example.salubris.utils.truncate2Decimals
import com.example.salubris.ui.theme.caloriesColor
import com.example.salubris.ui.theme.proteinColor
import com.example.salubris.ui.theme.carbsColor
import com.example.salubris.ui.theme.fatsColor

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

    var isOpen by remember { mutableStateOf(false) }
    var mealName by remember { mutableStateOf("") }
    val selectedProducts = remember { mutableStateListOf<ProductWithQuantity>() }

    var expanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<com.example.salubris.database.entities.Product?>(null) }
    var quantityInput by remember { mutableStateOf("100") }

    LaunchedEffect(Unit) {
        mealViewModel.loadData()
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
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Meal", color = Color.White)
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

        Modal(
            open = isOpen,
            onClose = {
                isOpen = false
                mealName = ""
                selectedProducts.clear()
                selectedProduct = null
                quantityInput = "100"
            },
            title = "Add a meal",
            onSubmit = {
                if (mealName.isNotBlank() && selectedProducts.isNotEmpty()) {
                    scope.launch {
                        mealViewModel.addMeal(mealName, selectedProducts.toList())
                        isOpen = false
                        mealName = ""
                        selectedProducts.clear()
                        selectedProduct = null
                        quantityInput = "100"
                    }
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Meal name
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Meal name", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    )
                )

                Text("Products", fontWeight = FontWeight.Bold, color = Color.White)

                // Show selected products count for debugging
                Text(
                    text = "Selected products: ${selectedProducts.size}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                // Highlighted selected product rows
                selectedProducts.forEachIndexed { index, productWithQty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(55, 55, 55, 255),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                productWithQty.product.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Quantity: ${productWithQty.quantity}g",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { selectedProducts.removeAt(index) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove",
                                tint = cancelColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Product picker dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select a product") },
                        trailingIcon = {
                            if (selectedProduct != null) {
                                IconButton(onClick = { selectedProduct = null }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.White
                                    )
                                }
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            cursorColor = Color.White
                        ),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(ContainerBackground)
                            .border(0.5.dp, Color.White)
                    ) {
                        if (products.isNotEmpty()) {
                            products.forEach { product ->
                                DropdownMenuItem(
                                    text = { Text(product.name, color = Color.White) },
                                    onClick = {
                                        selectedProduct = product
                                        expanded = false
                                        Log.d("Meals", "Selected product: ${product.name}")
                                    },
                                    modifier = Modifier.background(ContainerBackground)
                                )
                            }
                        } else {
                            DropdownMenuItem(
                                text = { Text("No products", color = Color.Gray) },
                                onClick = { expanded = false }
                            )
                        }
                    }
                }

                // Quantity input (numeric)
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    label = { Text("Quantity (g)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    )
                )

                // Nutrition preview
                if (selectedProduct != null) {
                    ProductNutritionLabel(selectedProduct!!)
                }

                Button(
                    onClick = {
                        val quantity = quantityInput.toFloatOrNull()
                        if (selectedProduct != null && quantity != null && quantity > 0) {
                            // Always add a new entry, even if same product already exists
                            selectedProducts.add(ProductWithQuantity(selectedProduct!!, quantity))
                            selectedProduct = null
                            quantityInput = "100"
                        }
                    },
                    enabled = selectedProduct != null && quantityInput.toFloatOrNull() != null,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = productColor,
                        disabledContainerColor = productColor.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(24.dp), tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("ADD PRODUCT TO MEAL", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MealItem(
    mealWithProducts: MealWithProducts,
    onDelete: () -> Unit
) {
    // Calculate total macros for the whole meal (as built)
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

    // Per 100g values
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
            // Meal name
            Text(
                mealWithProducts.meal.name,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )
            // Metadata: number of ingredients & total weight
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
            // Macros per 100g
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("K: ${per100Calories.truncate2Decimals()}", color = caloriesColor, fontSize = 14.sp)
                Text("P: ${per100Protein.truncate2Decimals()}", color = proteinColor, fontSize = 14.sp)
                Text("C: ${per100Carbs.truncate2Decimals()}", color = carbsColor, fontSize = 14.sp)
                Text("F: ${per100Fats.truncate2Decimals()}", color = fatsColor, fontSize = 14.sp)
            }
            Text("per 100g", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        }
        // Delete button
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

@Composable
fun mealViewModelFactory(context: android.content.Context): MealViewModelFactory {
    val database = AppDatabase.getDatabase(context)
    val repository = MealRepository(database.mealDao())
    return MealViewModelFactory(repository)
}