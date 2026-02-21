package com.example.salubris.database.repositories

import com.example.salubris.database.DAO.SettingDao
import com.example.salubris.database.entities.Setting
import kotlinx.coroutines.flow.Flow

class SettingRepository(
    private val settingDao: SettingDao
) {
    suspend fun insertSetting(setting: Setting) {
        settingDao.insert(setting)
    }

    suspend fun insertSettings(settings: List<Setting>) {
        settingDao.insertAll(settings)
    }

    fun getAllSettings(): Flow<List<Setting>> {
        return settingDao.getAllSettings()
    }

    suspend fun getSettingByName(name: String): Setting? {
        return settingDao.getSettingByName(name)
    }

    fun getSettingFlowById(id: Int): Flow<Setting?> {
        return settingDao.getSettingByIdFlow(id)
    }

    suspend fun getSettingById(id: Int): Setting? {
        return settingDao.getSettingById(id)
    }

    suspend fun updateSetting(setting: Setting) {
        settingDao.update(setting)
    }

    suspend fun deleteSetting(setting: Setting) {
        settingDao.delete(setting)
    }

    suspend fun deleteAllSettings() {
        settingDao.deleteAll()
    }
}