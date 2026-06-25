package com.example.salubris.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    val name: String,
    val componentsJson: String // Serialized List<MealComponent>
)
