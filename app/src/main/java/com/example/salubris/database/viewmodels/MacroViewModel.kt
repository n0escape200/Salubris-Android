package com.example.salubris.database.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.MacroEntity
import com.example.salubris.database.entities.MealComponent
import com.example.salubris.database.repositories.MacroRepository
import com.example.salubris.database.repositories.MealRepository
import com.example.salubris.database.viewmodels.SettingViewModel.OperationStatus
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class TrackedItem(
    val id: Int,
    val type: String,          // "product" or "meal" – derived from isMeal
    val name: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fats: Float,
    val amountOrMultiplier: Float,
    val date: Long,
    val isMeal: Boolean
)

class MacroViewModel(
    private val macroRepository: MacroRepository,
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    private val _error = MutableStateFlow<String?>(null)

    fun saveMacroLine(
        name: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fats: Float,
        amount: Float,
        date: Long
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationStatus.value = OperationStatus.Idle
            try {
                val newMacroLine = MacroEntity(
                    name = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fats = fats,
                    amount = amount,
                    date = date,
                    isMeal = false   // ✅ product
                )
                macroRepository.insertMacroLine(newMacroLine)
                _operationStatus.value = OperationStatus.Success
            } catch (e: Exception) {
                _error.value = "Error saving product: ${e.message}"
                _operationStatus.value = OperationStatus.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveTrackedMeal(mealId: Int, consumedGrams: Float, date: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val meal = mealRepository.getMealById(mealId)
                if (meal == null) {
                    _error.value = "Meal not found"
                    return@launch
                }
                val components =
                    Gson().fromJson(meal.componentsJson, Array<MealComponent>::class.java).toList()
                val totalRecipeWeight = components.sumOf { it.quantity.toDouble() }.toFloat()
                val fraction = if (totalRecipeWeight > 0) consumedGrams / totalRecipeWeight else 0f

                var totalCalories = 0f
                var totalProtein = 0f
                var totalCarbs = 0f
                var totalFats = 0f

                components.forEach { comp ->
                    val factor = comp.quantity * fraction / 100f
                    totalCalories += comp.calories * factor
                    totalProtein += comp.protein * factor
                    totalCarbs += comp.carbs * factor
                    totalFats += comp.fats * factor
                }

                val macroEntry = MacroEntity(
                    name = meal.name,
                    calories = totalCalories,
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fats = totalFats,
                    amount = consumedGrams,
                    date = date,
                    isMeal = true   // ✅ meal
                )
                macroRepository.insertMacroLine(macroEntry)
                _operationStatus.value = OperationStatus.Success
            } catch (e: Exception) {
                _error.value = "Error saving meal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Returns combined tracked items (now all are Macros) for a given day.
     */
    suspend fun getTrackedItemsForDay(dayStart: Long): List<TrackedItem> {
        val productEntries = macroRepository.getMacrosForDay(dayStart)

        return productEntries.map { macro ->
            TrackedItem(
                id = macro.uid,
                type = if (macro.isMeal) "meal" else "product",
                name = macro.name,
                calories = macro.calories,
                protein = macro.protein,
                carbs = macro.carbs,
                fats = macro.fats,
                amountOrMultiplier = macro.amount,
                date = macro.date,
                isMeal = macro.isMeal
            )
        }
    }

    suspend fun deleteTrackedProduct(macro: MacroEntity) {
        macroRepository.deleteMacro(macro)
    }

    suspend fun deleteMacroById(id: Int) {
        macroRepository.deleteMacroById(id)
    }
}

// Update MacroRepository to have deleteById
// I'll provide the updated repository file as well.

class MacroViewModelFactory(
    private val macroRepository: MacroRepository,
    private val mealRepository: MealRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MacroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MacroViewModel(macroRepository, mealRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun macroViewModelFactory(context: android.content.Context): MacroViewModelFactory {
    val database = AppDatabase.getDatabase(context)
    val macroRepository = MacroRepository(database.macroDao())
    val mealRepository = MealRepository(database.mealDao())
    return MacroViewModelFactory(macroRepository, mealRepository)
}