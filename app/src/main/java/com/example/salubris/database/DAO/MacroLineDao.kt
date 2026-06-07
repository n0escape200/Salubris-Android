package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.salubris.database.entities.Macro

@Dao
interface MacroLineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(macro: Macro)

    @Query("SELECT * FROM Macro")
    suspend fun getAllLines(): List<Macro>

    @Transaction  // Required for @Relation queries
    @Query(
        """
        SELECT * FROM Macro
        WHERE date >= :dayStart
        AND date < (:dayStart + 86400000)
        """
    )
    suspend fun getMacrosForDay(dayStart: Long): List<Macro>

    @Delete
    suspend fun delete(macro: Macro)
}