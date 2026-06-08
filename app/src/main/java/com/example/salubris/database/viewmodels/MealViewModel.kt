package com.example.salubris.database.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.DAO.MealDao
import com.example.salubris.database.DAO.TrackedMealDao
import com.example.salubris.database.entities.Meal
import com.example.salubris.database.entities.MealWithProducts
import com.example.salubris.database.entities.Product
import com.example.salubris.database.relations.ProductWithQuantity   // ✅ fixed import
import com.example.salubris.database.repositories.MacroRepository
import com.example.salubris.database.repositories.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealViewModel(
    private val repository: MealRepository
) : ViewModel() {

    private val _mealsWithProducts = MutableStateFlow<List<MealWithProducts>>(emptyList())
    val mealsWithProducts: StateFlow<List<MealWithProducts>> = _mealsWithProducts.asStateFlow()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _mealsWithProducts.value = repository.getAllMealsWithProducts()
                _allProducts.value = repository.getAllProducts()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addMeal(mealName: String, productsWithQuantities: List<ProductWithQuantity>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.insertMealWithProducts(mealName, productsWithQuantities)
                _mealsWithProducts.value = repository.getAllMealsWithProducts()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMealProducts(mealId: Int, productsWithQuantities: List<ProductWithQuantity>) {
        viewModelScope.launch {
            repository.updateMealProducts(mealId, productsWithQuantities)
            _mealsWithProducts.value = repository.getAllMealsWithProducts()
        }
    }

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
            _mealsWithProducts.value = repository.getAllMealsWithProducts()
        }
    }
}

class MealViewModelFactory(
    private val repository: MealRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun mealViewModelFactory(context: android.content.Context): MealViewModelFactory {
    val database = AppDatabase.getDatabase(context)
    val mealRepository = MealRepository(database.mealDao())
    return MealViewModelFactory(mealRepository)
}