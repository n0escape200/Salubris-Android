package com.example.salubris.database.repositories;

import com.example.salubris.database.DAO.MacroLineDao;
import com.example.salubris.database.entities.Macro

class MacroRepository(private val macroDao:MacroLineDao) {
    suspend fun insertMacroLine(macro: Macro){
        macroDao.insert(macro)
    }

    suspend fun getMacrosForDay(dayStart: Long): List<Macro> {
        return macroDao.getMacrosForDay(dayStart)
    }

    suspend fun deleteMacro(macro: Macro) {
        macroDao.delete(macro)
    }
}