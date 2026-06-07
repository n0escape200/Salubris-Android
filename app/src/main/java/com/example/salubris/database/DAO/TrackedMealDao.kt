package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.salubris.database.entities.TrackedMeal

@Dao
interface TrackedMealDao {
    @Insert
    suspend fun insert(trackedMeal: TrackedMeal)

    @Transaction
    @Query("SELECT * FROM TrackedMeal WHERE date >= :dayStart AND date < (:dayStart + 86400000)")
    suspend fun getTrackedMealsForDay(dayStart: Long): List<TrackedMeal>

    @Delete
    suspend fun delete(trackedMeal: TrackedMeal)
}