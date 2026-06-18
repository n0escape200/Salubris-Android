package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.salubris.database.entities.DailyWaterHistory
import com.example.salubris.database.entities.WaterEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Insert
    suspend fun insertEntry(entry: WaterEntry)

    @Delete
    suspend fun deleteEntry(entry: WaterEntry)

    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getEntriesForDate(date: String): Flow<List<WaterEntry>>

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE date = :date")
    fun getTotalForDate(date: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyHistory(history: DailyWaterHistory)

    @Query("SELECT * FROM daily_water_history WHERE date = :date")
    suspend fun getDailyHistory(date: String): DailyWaterHistory?

    @Query("SELECT * FROM daily_water_history ORDER BY date DESC")
    fun getAllHistory(): Flow<List<DailyWaterHistory>>
}