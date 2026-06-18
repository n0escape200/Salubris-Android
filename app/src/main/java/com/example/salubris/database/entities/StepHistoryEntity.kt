package com.example.salubris.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_history")
data class StepHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo(name = "date")
    val date: String,          // yyyy-MM-dd
    @ColumnInfo(name = "steps")
    val steps: Int
)