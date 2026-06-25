package com.example.salubris.database.repositories

import com.example.salubris.database.DAO.SettingDao
import com.example.salubris.database.entities.SettingEntity
import kotlinx.coroutines.flow.Flow

class SettingRepository(
    private val settingDao: SettingDao
) {
    suspend fun insertSetting(setting: SettingEntity) {
        settingDao.insert(setting)
    }

    suspend fun insertSettings(settings: List<SettingEntity>) {
        settingDao.insertAll(settings)
    }

    fun getAllSettings(): Flow<List<SettingEntity>> {
        return settingDao.getAllSettings()
    }

    suspend fun getSettingByName(name: String): SettingEntity? {
        return settingDao.getSettingByName(name)
    }

    fun getSettingFlowById(id: Int): Flow<SettingEntity?> {
        return settingDao.getSettingByIdFlow(id)
    }

    suspend fun getSettingById(id: Int): SettingEntity? {
        return settingDao.getSettingById(id)
    }

    suspend fun updateSetting(setting: SettingEntity) {
        settingDao.update(setting)
    }

    suspend fun deleteSetting(setting: SettingEntity) {
        settingDao.delete(setting)
    }

    suspend fun deleteAllSettings() {
        settingDao.deleteAll()
    }
}
