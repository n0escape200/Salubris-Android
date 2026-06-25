package com.example.salubris.database.viewmodels

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.DailyWaterHistoryEntity
import com.example.salubris.database.entities.WaterEntity
import com.example.salubris.database.repositories.WaterRepository
import com.example.salubris.database.viewmodels.SettingViewModel.OperationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WaterViewModel(
    private val repository: WaterRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val operationStatus: StateFlow<OperationStatus> = _operationStatus.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _todayEntries = MutableStateFlow<List<WaterEntity>>(emptyList())
    val todayEntries: StateFlow<List<WaterEntity>> = _todayEntries.asStateFlow()

    private val _todayTotal = MutableStateFlow(0)
    val todayTotal: StateFlow<Int> = _todayTotal.asStateFlow()

    private var currentDate: String = ""

    fun setDate(date: String) {
        if (currentDate != date) {
            currentDate = date
            loadDataForDate(date)
        }
    }

    private fun loadDataForDate(date: String) {
        viewModelScope.launch {
            repository.getTodayEntries(date).collect { entries ->
                _todayEntries.value = entries
            }
        }
        viewModelScope.launch {
            repository.getTodayTotal(date).collect { total ->
                _todayTotal.value = total ?: 0
            }
        }
    }

    private val _history = MutableStateFlow<List<DailyWaterHistoryEntity>>(emptyList())
    val history: StateFlow<List<DailyWaterHistoryEntity>> = _history.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            repository.getAllHistory().collect { historyList ->
                _history.value = historyList
            }
        }
    }

    fun addWaterEntry(amountMl: Int, date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationStatus.value = OperationStatus.Idle
            try {
                repository.addEntry(amountMl, date)
                _operationStatus.value = OperationStatus.Success
                loadDataForDate(date)
            } catch (e: Exception) {
                _error.value = "Error adding water entry: ${e.message}"
                _operationStatus.value = OperationStatus.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteWaterEntry(entry: WaterEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationStatus.value = OperationStatus.Idle
            try {
                repository.deleteEntry(entry)
                _operationStatus.value = OperationStatus.Success
                loadDataForDate(currentDate)
            } catch (e: Exception) {
                _error.value = "Error deleting water entry: ${e.message}"
                _operationStatus.value = OperationStatus.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun archiveDay(date: String, totalMl: Int, goalMl: Int) {
        viewModelScope.launch {
            repository.archiveDay(date, totalMl, goalMl)
        }
    }
}

class WaterViewModelFactory(private val repository: WaterRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun waterViewModelFactory(context: Context): WaterViewModelFactory {
    val database = AppDatabase.getDatabase(context)
    val repository = WaterRepository(database.waterDao())
    return WaterViewModelFactory(repository)
}
