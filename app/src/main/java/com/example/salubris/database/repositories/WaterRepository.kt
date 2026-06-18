package com.example.salubris.database.repositories

import com.example.salubris.database.DAO.WaterDao
import com.example.salubris.database.entities.DailyWaterHistory
import com.example.salubris.database.entities.WaterEntry

class WaterRepository(private val dao: WaterDao) {
    fun getTodayEntries(date: String) = dao.getEntriesForDate(date)
    fun getTodayTotal(date: String) = dao.getTotalForDate(date)
    suspend fun addEntry(amountMl: Int, date: String) {
        val entry =
            WaterEntry(amountMl = amountMl, timestamp = System.currentTimeMillis(), date = date)
        dao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: WaterEntry) = dao.deleteEntry(entry)
    suspend fun archiveDay(date: String, totalMl: Int) {
        dao.insertDailyHistory(DailyWaterHistory(date, totalMl))
    }

    fun getAllHistory() = dao.getAllHistory()
}