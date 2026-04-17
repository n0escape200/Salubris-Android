package com.example.salubris.database.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.entities.Setting
import com.example.salubris.database.repositories.SettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SettingViewModel(
    private val settingRepository: SettingRepository
) : ViewModel() {

    // State for all settings
    private val _settings = MutableStateFlow<List<Setting>>(emptyList())
    val settings: StateFlow<List<Setting>> = _settings.asStateFlow()

    // State for loading status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for error messages
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Operation status
    private val _operationStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val operationStatus: StateFlow<OperationStatus> = _operationStatus.asStateFlow()

    sealed class OperationStatus {
        object Idle : OperationStatus()
        object Success : OperationStatus()
        data class Error(val message: String) : OperationStatus()
    }

    init {
        getAllSettings()
    }

    // READ all settings as Flow
    fun getAllSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            settingRepository.getAllSettings()
                .catch { exception ->
                    _error.value = "Error loading settings: ${exception.message}"
                    _isLoading.value = false
                }
                .collect { settingsList ->
                    _settings.value = settingsList
                    _isLoading.value = false
                }
        }
    }

    // READ single setting by name
    fun getSettingByName(name: String): Setting? {
        return _settings.value.find { it.name == name }
    }

    // READ setting value as string
    fun getSettingValue(name: String, defaultValue: String = ""): String {
        return getSettingByName(name)?.value ?: defaultValue
    }

    // READ setting value as int
    fun getSettingValueAsInt(name: String, defaultValue: Int = 0): Int {
        return getSettingByName(name)?.value?.toIntOrNull() ?: defaultValue
    }

    // CREATE or UPDATE setting
    fun saveSetting(name: String, value: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationStatus.value = OperationStatus.Idle
            try {
                val existingSetting = getSettingByName(name)
                if (existingSetting != null) {
                    val updatedSetting = existingSetting.copy(value = value)
                    settingRepository.updateSetting(updatedSetting)
                } else {
                    val newSetting = Setting(name = name, value = value)
                    settingRepository.insertSetting(newSetting)
                }
                _operationStatus.value = OperationStatus.Success
            } catch (e: Exception) {
                _error.value = "Error saving setting: ${e.message}"
                _operationStatus.value = OperationStatus.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // CREATE multiple settings
    fun saveSettings(settings: List<Pair<String, String>>) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationStatus.value = OperationStatus.Idle
            try {
                val settingEntities = settings.map { (name, value) ->
                    val existing = getSettingByName(name)
                    if (existing != null) {
                        existing.copy(value = value)
                    } else {
                        Setting(name = name, value = value)
                    }
                }
                settingRepository.insertSettings(settingEntities)
                _operationStatus.value = OperationStatus.Success
            } catch (e: Exception) {
                _error.value = "Error saving settings: ${e.message}"
                _operationStatus.value = OperationStatus.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // DELETE setting by name
    fun deleteSetting(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getSettingByName(name)?.let { setting ->
                    settingRepository.deleteSetting(setting)
                }
                _operationStatus.value = OperationStatus.Success
            } catch (e: Exception) {
                _error.value = "Error deleting setting: ${e.message}"
                _operationStatus.value = OperationStatus.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // DELETE all settings
    fun deleteAllSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                settingRepository.deleteAllSettings()
                _operationStatus.value = OperationStatus.Success
            } catch (e: Exception) {
                _error.value = "Error deleting all settings: ${e.message}"
                _operationStatus.value = OperationStatus.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetOperationStatus() {
        _operationStatus.value = OperationStatus.Idle
    }

    fun getSettingsMap(): Map<String, String> {
        return _settings.value.associate { it.name to it.value }
    }
}

// Factory class for creating ViewModel with dependencies
class SettingViewModelFactory(
    private val repository: SettingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Composable
fun settingsViewModelFactory(context: android.content.Context): SettingViewModelFactory {
    val database = AppDatabase.getDatabase(context)
    val repository = SettingRepository(database.settingDao())
    return SettingViewModelFactory(repository)
}