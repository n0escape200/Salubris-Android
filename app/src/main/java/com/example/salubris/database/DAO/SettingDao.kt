package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.salubris.database.entities.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: Setting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<Setting>)

    // READ ALL - Fixed: now queries from settings table
    @Query("SELECT * FROM settings ORDER BY name ASC")
    fun getAllSettings(): Flow<List<Setting>>

    // READ by name
    @Query("SELECT * FROM settings WHERE name = :name")
    suspend fun getSettingByName(name: String): Setting?

    // READ by id as Flow
    @Query("SELECT * FROM settings WHERE uid = :id")
    fun getSettingByIdFlow(id: Int): Flow<Setting?>

    // READ by id as suspend
    @Query("SELECT * FROM settings WHERE uid = :id")
    suspend fun getSettingById(id: Int): Setting?

    // UPDATE
    @Update
    suspend fun update(setting: Setting)

    // DELETE
    @Delete
    suspend fun delete(setting: Setting)

    @Query("DELETE FROM settings")
    suspend fun deleteAll()
}