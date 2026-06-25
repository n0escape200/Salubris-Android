package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.salubris.database.entities.DailyWaterHistoryEntity
import com.example.salubris.database.entities.WaterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Insert
    suspend fun insertEntry(entry: WaterEntity)

    @Delete
    suspend fun deleteEntry(entry: WaterEntity)

    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getEntriesForDate(date: String): Flow<List<WaterEntity>>

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE date = :date")
    fun getTotalForDate(date: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyHistory(history: DailyWaterHistoryEntity)

    @Query("SELECT * FROM daily_water_history WHERE date = :date")
    suspend fun getDailyHistory(date: String): DailyWaterHistoryEntity?

    @Query("SELECT * FROM daily_water_history ORDER BY date DESC")
    fun getAllHistory(): Flow<List<DailyWaterHistoryEntity>>

    @Query("SELECT * FROM daily_water_history WHERE date = :date")
    suspend fun getByDate(date: String): DailyWaterHistoryEntity?
}
