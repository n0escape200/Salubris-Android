package com.example.salubris.database.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.MealComponent
import com.example.salubris.database.entities.MealEntity
import com.example.salubris.database.repositories.MealRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Helper for UI to handle parsed meals
data class MealUI(
    val meal: MealEntity,
    val components: List<MealComponent>
)

class MealViewModel(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _meals = MutableStateFlow<List<MealUI>>(emptyList())
    val meals: StateFlow<List<MealUI>> = _meals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val gson = Gson()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val mealsList = mealRepository.getAllMeals()
                _meals.value = mealsList.map {
                    MealUI(
                        it,
                        gson.fromJson(it.componentsJson, Array<MealComponent>::class.java).toList()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addMeal(mealName: String, components: List<MealComponent>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val meal = MealEntity(
                    name = mealName,
                    componentsJson = gson.toJson(components)
                )
                mealRepository.insertMeal(meal)
                loadData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ NEW: update an existing meal
    fun updateMeal(mealId: Int, newName: String, newComponents: List<MealComponent>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existingMeal = mealRepository.getMealById(mealId)
                if (existingMeal != null) {
                    val updatedMeal = existingMeal.copy(
                        name = newName,
                        componentsJson = gson.toJson(newComponents)
                    )
                    // Note: we need an update method in repository; we'll add one.
                    mealRepository.updateMeal(updatedMeal)
                    loadData()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMeal(meal: MealEntity) {
        viewModelScope.launch {
            mealRepository.deleteMeal(meal)
            loadData()
        }
    }
}

// Update MealRepository to include update method:
// In MealRepository.kt, add:
// suspend fun updateMeal(meal: MealEntity) = mealDao.updateMeal(meal)

class MealViewModelFactory(
    private val mealRepository: MealRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealViewModel(mealRepository) as T
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