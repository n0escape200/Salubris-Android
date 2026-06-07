package com.example.salubris.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["uid"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
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
    @ColumnInfo("isDraft") val isDraft: Boolean = false,
)
