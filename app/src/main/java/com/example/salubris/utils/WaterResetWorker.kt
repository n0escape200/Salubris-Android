package com.example.salubris.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.salubris.database.repositories.SettingRepository
import com.example.salubris.database.repositories.WaterRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class WaterResetWorker(
    context: Context,
    params: WorkerParameters,
    private val waterRepository: WaterRepository,
    private val settingRepository: SettingRepository
) : CoroutineWorker(context, params) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val yesterday = LocalDate.now().minusDays(1).toString()
        val total = waterRepository.getTodayTotal(yesterday).first() ?: 0

        // Get water goal from settings
        val goalSetting = settingRepository.getSettingByName("goal_water")
        val goalMl = goalSetting?.value?.toIntOrNull() ?: 2000   // fallback default

        if (total > 0) {
            waterRepository.archiveDay(yesterday, total, goalMl)
            // Optionally delete yesterday's entries
            val entries = waterRepository.getTodayEntries(yesterday).first()
            entries.forEach { waterRepository.deleteEntry(it) }
        }
        return Result.success()
    }
}