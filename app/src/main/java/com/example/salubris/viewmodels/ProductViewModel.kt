package com.example.salubris.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.entities.Product
import com.example.salubris.database.repositories.ProductRepository
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