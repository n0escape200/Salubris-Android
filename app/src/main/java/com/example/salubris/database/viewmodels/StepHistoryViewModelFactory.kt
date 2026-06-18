package com.example.salubris.database.viewmodels

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.salubris.database.AppDatabase
import com.example.salubris.database.repositories.StepHistoryRepository

class StepHistoryViewModelFactory(
    private val repository: StepHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StepHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun stepHistoryViewModelFactory(context: android.content.Context): StepHistoryViewModelFactory {
    val database = AppDatabase.getDatabase(context)
    val repository = StepHistoryRepository(database.stepHistoryDao())
    return StepHistoryViewModelFactory(repository)
}