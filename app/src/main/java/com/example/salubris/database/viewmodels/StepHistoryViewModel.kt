package com.example.salubris.database.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salubris.database.entities.StepHistoryEntity
import com.example.salubris.database.repositories.StepHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class StepHistoryViewModel(
    private val repository: StepHistoryRepository
) : ViewModel() {

    private val PAGE_SIZE = 10

    private val _history = MutableStateFlow<List<StepHistoryEntity>>(emptyList())
    val history: StateFlow<List<StepHistoryEntity>> = _history.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private var currentPage = 0

    init {
        loadNextPage()
    }

    fun refresh() {
        currentPage = 0
        _history.value = emptyList()
        _canLoadMore.value = true
        loadNextPage()
    }

    fun loadNextPage() {
        if (_isLoading.value || !_canLoadMore.value) return

        viewModelScope.launch {
            _isLoading.value = true
            val offset = currentPage * PAGE_SIZE

            repository.getHistoryPaged(PAGE_SIZE, offset)
                .catch { exception ->
                    // handle error
                    _isLoading.value = false
                }
                .collect { newItems ->
                    val currentList = _history.value
                    _history.value = (currentList + newItems).distinctBy { it.uid }

                    // Check if we reached the end
                    if (newItems.size < PAGE_SIZE) {
                        _canLoadMore.value = false
                    } else {
                        // Verify total count to be safe
                        val total = repository.getTotalCount()
                        if (_history.value.size >= total) {
                            _canLoadMore.value = false
                        }
                    }
                    currentPage++
                    _isLoading.value = false
                }
        }
    }
}