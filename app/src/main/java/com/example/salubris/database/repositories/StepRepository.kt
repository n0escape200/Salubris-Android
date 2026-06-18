package com.example.salubris.database.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StepRepository {
    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private val _sensorAvailable = MutableStateFlow(true)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable.asStateFlow()

    fun updateSteps(value: Int) {
        _steps.value = value
    }

    fun setSensorAvailable(available: Boolean) {
        _sensorAvailable.value = available
    }
}