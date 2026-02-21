package com.example.salubris.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.Product
import com.example.salubris.database.repositories.ProductRepository
import com.example.salubris.database.repositories.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductViewModel(
    private val repository: ProductRepository
): ViewModel() {
    private var products = MutableStateFlow<List<Product>>(emptyList())
    init {

    }

    private fun getProducts(){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                repository.getAllProducts()
            }
            products = data as MutableStateFlow<List<Product>>;
        }
    }
}


class ProductViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Composable
fun productViewModelFactory(context: android.content.Context): ProductViewModelFactory {
    val database = AppDatabase.getDatabase(context)
    val repository = ProductRepository(database.productDao())
    return ProductViewModelFactory(repository)
}