package com.example.salubris.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.salubris.database.repositories.WaterRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class WaterResetWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: WaterRepository
) : CoroutineWorker(context, params) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val yesterday = LocalDate.now().minusDays(1).toString()
        val total = repository.getTodayTotal(yesterday).first() ?: 0
        if (total > 0) {
            repository.archiveDay(yesterday, total)
            // Optionally delete yesterday's entries
            val entries = repository.getTodayEntries(yesterday).first()
            entries.forEach { repository.deleteEntry(it) }
        }
        return Result.success()
    }
}