package com.example.salubris.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.salubris.database.entities.Meal
import com.example.salubris.database.entities.TrackedMeal

data class TrackedMealWithMeal(
    @Embedded val trackedMeal: TrackedMeal,
    @Relation(
        parentColumn = "mealId",
        entityColumn = "uid"
    )
    val meal: Meal?
)