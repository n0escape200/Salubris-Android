package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.salubris.database.entities.Macro
import com.example.salubris.database.relations.MacroWithProduct

@Dao
interface MacroLineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(macro: Macro)

    @Query("SELECT * FROM Macro")
    suspend fun getAllLines(): List<Macro>

    @Query(
        """
    SELECT * FROM Macro
    WHERE date >= :dayStart
    AND date < (:dayStart + 86400000)
"""
    )
    suspend fun getMacrosForDay(dayStart: Long): List<MacroWithProduct>
}