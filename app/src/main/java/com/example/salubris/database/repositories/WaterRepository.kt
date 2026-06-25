package com.example.salubris.database.repositories

import com.example.salubris.database.DAO.WaterDao
import com.example.salubris.database.entities.DailyWaterHistoryEntity
import com.example.salubris.database.entities.WaterEntity

class WaterRepository(private val dao: WaterDao) {
    fun getTodayEntries(date: String) = dao.getEntriesForDate(date)
    fun getTodayTotal(date: String) = dao.getTotalForDate(date)
    
    suspend fun addEntry(amountMl: Int, date: String) {
        val entry = WaterEntity(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            date = date
        )
        dao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: WaterEntity) = dao.deleteEntry(entry)

    suspend fun archiveDay(date: String, totalMl: Int, goalMl: Int) {
        dao.insertDailyHistory(DailyWaterHistoryEntity(date, totalMl, goalMl))
    }

    fun getAllHistory() = dao.getAllHistory()
}
