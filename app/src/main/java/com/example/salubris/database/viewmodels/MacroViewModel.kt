package com.example.salubris.database.viewmodels

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.DAO.MealDao
import com.example.salubris.database.DAO.TrackedMealDao
import com.example.salubris.database.entities.Macro
import com.example.salubris.database.entities.TrackedMeal
import com.example.salubris.database.repositories.MacroRepository
import com.example.salubris.database.viewmodels.SettingViewModel.OperationStatus
import com.example.salubris.utils.calculateMacrosForProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class TrackedItem(
    val id: Int,
    val type: String,          // "product" or "meal"
    val name: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fats: Float,
    val originalId: Int,
    val amountOrMultiplier: Float,
    val date: Long
)

class MacroViewModel(
    private val macroRepository: MacroRepository,
    private val trackedMealDao: TrackedMealDao,
    private val mealDao: MealDao
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    private val _error = MutableStateFlow<String?>(null)

    fun saveMacroLine(productId: Int, amount: Float, date: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationStatus.value = OperationStatus.Idle
            try {
                val newMacroLine = Macro(productId = productId, amount = amount, date = date)
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

    fun saveMeal(mealId: Int, quantityGrams: Float, date: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val trackedMeal = TrackedMeal(
                    mealId = mealId,
                    consumedGrams = quantityGrams,
                    date = date
                )
                trackedMealDao.insert(trackedMeal)
                _operationStatus.value = OperationStatus.Success
            } catch (e: Exception) {
                _error.value = "Error saving meal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Returns combined tracked items (products + meals) for a given day.
     */
    suspend fun getTrackedItemsForDay(dayStart: Long): List<TrackedItem> {
        val productEntries = macroRepository.getMacrosForDay(dayStart)
        val mealEntries = trackedMealDao.getTrackedMealsForDay(dayStart)

        val productItems = productEntries.mapNotNull { (macro, product) ->
            if (product != null) {
                val macros = calculateMacrosForProduct(product, macro.amount)
                TrackedItem(
                    id = macro.uid,
                    type = "product",
                    name = product.name,
                    calories = macros["calories"]!!,
                    protein = macros["protein"]!!,
                    carbs = macros["carbs"]!!,
                    fats = macros["fats"]!!,
                    originalId = product.uid,
                    amountOrMultiplier = macro.amount,
                    date = macro.date
                )
            } else null
        }

        val mealItems = mealEntries.mapNotNull { (trackedMeal, meal) ->
            if (meal != null) {
                val macros = calculateMealMacros(meal.uid, trackedMeal.consumedGrams)
                TrackedItem(
                    id = trackedMeal.uid,
                    type = "meal",
                    name = meal.name,
                    calories = macros["calories"] ?: 0f,
                    protein = macros["protein"] ?: 0f,
                    carbs = macros["carbs"] ?: 0f,
                    fats = macros["fats"] ?: 0f,
                    originalId = meal.uid,
                    amountOrMultiplier = trackedMeal.consumedGrams,
                    date = trackedMeal.date
                )
            } else null
        }

        return productItems + mealItems
    }

    suspend fun deleteTrackedProduct(macro: Macro) {
        macroRepository.deleteMacro(macro)
    }

    suspend fun deleteTrackedMeal(trackedMeal: TrackedMeal) {
        trackedMealDao.delete(trackedMeal)
    }

    suspend fun deleteMacroById(id: Int) {
        macroRepository.deleteMacro(Macro(uid = id, productId = 0, amount = 0f, date = 0L))
    }

    suspend fun deleteTrackedMealById(id: Int) {
        trackedMealDao.delete(TrackedMeal(uid = id, mealId = 0, consumedGrams = 0f, date = 0L))
    }

    private suspend fun calculateMealMacros(mealId: Int, consumedGrams: Float): Map<String, Float> {
        val mealWithProducts = mealDao.getMealWithProducts(mealId)
        Log.d("MealMacros", "Meal ID: $mealId, consumedGrams: $consumedGrams")
        Log.d("MealMacros", "Products count: ${mealWithProducts?.products?.size ?: 0}")

        var totalCalories = 0f
        var totalProtein = 0f
        var totalCarbs = 0f
        var totalFats = 0f

        if (mealWithProducts != null) {
            // Log each product in the meal
            mealWithProducts.products.forEachIndexed { index, productWithQty ->
                Log.d("MealMacros", "Product ${index+1}: ${productWithQty.product.name}, quantity in meal: ${productWithQty.quantity}g")
            }

            val totalWeight = mealWithProducts.products.sumOf { it.quantity.toDouble() }.toFloat()
            Log.d("MealMacros", "Total meal weight: $totalWeight g")
            val fraction = if (totalWeight > 0) consumedGrams / totalWeight else 0f
            Log.d("MealMacros", "Fraction (consumed/total): $fraction")

            mealWithProducts.products.forEach { productWithQty ->
                val amount = productWithQty.quantity * fraction
                val macros = calculateMacrosForProduct(productWithQty.product, amount)
                totalCalories += macros["calories"]!!
                totalProtein += macros["protein"]!!
                totalCarbs += macros["carbs"]!!
                totalFats += macros["fats"]!!
                Log.d("MealMacros", "  -> ${productWithQty.product.name}: amount=$amount g, calories=${macros["calories"]}")
            }
        } else {
            Log.e("MealMacros", "MealWithProducts is NULL for mealId: $mealId")
        }

        return mapOf(
            "calories" to totalCalories,
            "protein" to totalProtein,
            "carbs" to totalCarbs,
            "fats" to totalFats
        )
    }
}

class MacroViewModelFactory(
    private val macroRepository: MacroRepository,
    private val trackedMealDao: TrackedMealDao,
    private val mealDao: MealDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MacroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MacroViewModel(macroRepository, trackedMealDao, mealDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun macroViewModelFactory(context: android.content.Context): MacroViewModelFactory {
    val database = AppDatabase.getDatabase(context)
    val macroRepository = MacroRepository(database.macroDao())
    val trackedMealDao = database.trackedMealDao()
    val mealDao = database.mealDao()
    return MacroViewModelFactory(macroRepository, trackedMealDao, mealDao)
}