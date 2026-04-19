package com.example.salubris.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.salubris.database.relations.ProductWithQuantity

@Entity
data class MealProductCrossRef(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,          // new primary key
    val mealId: Int,
    val productId: Int,
    val quantity: Float = 0f
)

@Entity
data class Meal(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    val name: String
)

data class MealWithProducts(
    @Embedded val meal: Meal,
    val products: List<ProductWithQuantity>
)