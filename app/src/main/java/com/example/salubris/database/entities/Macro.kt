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
    @ColumnInfo("productId") val productId: Int,
    @ColumnInfo("amount") val amount: Float,
    @ColumnInfo("date") val date: Long
)
