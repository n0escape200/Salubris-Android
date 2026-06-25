package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.salubris.database.entities.MacroEntity

@Dao
interface MacroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(macro: MacroEntity)

    @Query("SELECT * FROM macro")   // table name lowercased to match entity
    suspend fun getAllLines(): List<MacroEntity>

    @Transaction
    @Query(
        """
        SELECT * FROM macro
        WHERE date >= :dayStart
        AND date < (:dayStart + 86400000)
        """
    )
    suspend fun getMacrosForDay(dayStart: Long): List<MacroEntity>

    @Delete
    suspend fun delete(macro: MacroEntity)

    @Query("DELETE FROM macro WHERE uid = :id")
    suspend fun deleteById(id: Int)
}