package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.salubris.database.entities.SettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: SettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<SettingEntity>)

    @Query("SELECT * FROM settings ORDER BY name ASC")
    fun getAllSettings(): Flow<List<SettingEntity>>

    @Query("SELECT * FROM settings WHERE name = :name")
    suspend fun getSettingByName(name: String): SettingEntity?

    @Query("SELECT * FROM settings WHERE uid = :id")
    fun getSettingByIdFlow(id: Int): Flow<SettingEntity?>

    @Query("SELECT * FROM settings WHERE uid = :id")
    suspend fun getSettingById(id: Int): SettingEntity?

    @Update
    suspend fun update(setting: SettingEntity)

    @Delete
    suspend fun delete(setting: SettingEntity)

    @Query("DELETE FROM settings")
    suspend fun deleteAll()
}
