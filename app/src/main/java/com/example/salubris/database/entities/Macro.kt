package com.example.salubris.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Macro(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "calories") val calories: Float = 0f,
    @ColumnInfo(name = "protein") val protein: Float = 0f,
    @ColumnInfo(name = "carbs") val carbs: Float = 0f,
    @ColumnInfo(name = "fats") val fats: Float = 0f,
    @ColumnInfo("amount") val amount: Float,
    @ColumnInfo("date") val date: Long,
)
