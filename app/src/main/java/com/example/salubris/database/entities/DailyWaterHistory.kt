package com.example.salubris.database.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_water_history")
data class DailyWaterHistory(
    @PrimaryKey val date: String,
    val totalMl: Int
)