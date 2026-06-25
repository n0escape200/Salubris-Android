package com.example.salubris.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_water_history")
data class DailyWaterHistoryEntity(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val consumedMl: Int,
    val goalMl: Int
)
