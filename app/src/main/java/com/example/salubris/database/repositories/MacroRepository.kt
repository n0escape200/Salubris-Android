package com.example.salubris.database.repositories

import com.example.salubris.database.DAO.MacroDao
import com.example.salubris.database.entities.MacroEntity

class MacroRepository(private val macroDao: MacroDao) {
    suspend fun insertMacroLine(macro: MacroEntity) {
        macroDao.insert(macro)
    }

    suspend fun getMacrosForDay(dayStart: Long): List<MacroEntity> {
        return macroDao.getMacrosForDay(dayStart)
    }

    suspend fun deleteMacro(macro: MacroEntity) {
        macroDao.delete(macro)
    }

    suspend fun deleteMacroById(id: Int) {
        macroDao.deleteById(id)
    }
}