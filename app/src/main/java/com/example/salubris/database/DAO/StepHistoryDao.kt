package com.example.salubris.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.salubris.database.entities.StepHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: StepHistoryEntity)

    @Query("SELECT * FROM step_history ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getHistoryPaged(limit: Int, offset: Int): Flow<List<StepHistoryEntity>>

    @Query("SELECT COUNT(*) FROM step_history")
    suspend fun getTotalCount(): Int

    @Query("SELECT * FROM step_history WHERE date = :date")
    suspend fun getByDate(date: String): StepHistoryEntity?
}