package com.example.salubris.viewmodels;

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.Macro
import com.example.salubris.database.relations.MacroWithProduct
import com.example.salubris.database.repositories.MacroRepository
import com.example.salubris.viewmodels.SettingViewModel.OperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MacroViewModel(
    private val repository: MacroRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    private val _error = MutableStateFlow<String?>(null)

    fun saveMacroLine(productId: String, amount: Float, date: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationStatus.value = OperationStatus.Idle
            try {
                val newMacroLine = Macro(productId = productId, amount = amount, date = date)
                repository.insertMacroLine(newMacroLine)
                _operationStatus.value = OperationStatus.Success
            } catch (e: Exception) {
                _error.value = "Error saving setting: ${e.message}"
                _operationStatus.value = OperationStatus.Error(e.message ?: "Unknown error")
            }finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getMacrosPerDay(day: Long): List<MacroWithProduct> {
        return repository.getMacrosForDay(day)
    }
}

class MacroViewModelFactory(
    private val repository: MacroRepository
): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MacroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MacroViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun macroViewModelFactory(context: android.content.Context): MacroViewModelFactory {
    val database = AppDatabase.getDatabase(context)
    val repository = MacroRepository(database.macroDao())
    return MacroViewModelFactory(repository)
}
