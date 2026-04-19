package com.example.salubris.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Meal::class,
            parentColumns = ["uid"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mealId"), Index("date")]
)
data class TrackedMeal(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo("mealId") val mealId: Int,
    @ColumnInfo("consumedGrams") val consumedGrams: Float,
    @ColumnInfo("date") val date: Long
)