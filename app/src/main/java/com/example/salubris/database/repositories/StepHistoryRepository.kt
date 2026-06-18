package com.example.salubris.database.repositories

import com.example.salubris.database.dao.StepHistoryDao
import com.example.salubris.database.entities.StepHistoryEntity
import kotlinx.coroutines.flow.Flow

class StepHistoryRepository(
    private val stepHistoryDao: StepHistoryDao
) {
    suspend fun insertStepHistory(entry: StepHistoryEntity) {
        stepHistoryDao.insert(entry)
    }

    fun getHistoryPaged(limit: Int, offset: Int): Flow<List<StepHistoryEntity>> {
        return stepHistoryDao.getHistoryPaged(limit, offset)
    }

    suspend fun getTotalCount(): Int {
        return stepHistoryDao.getTotalCount()
    }
}