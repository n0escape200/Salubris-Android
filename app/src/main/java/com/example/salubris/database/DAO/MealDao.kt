package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update   // ✅ add this import
import com.example.salubris.database.entities.MealEntity

@Dao
interface MealDao {
    @Insert
    suspend fun insertMeal(meal: MealEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Query("SELECT * FROM meals ORDER BY uid DESC")
    suspend fun getAllMeals(): List<MealEntity>

    @Query("SELECT * FROM meals WHERE uid = :mealId")
    suspend fun getMealById(mealId: Int): MealEntity?

    @Delete
    suspend fun deleteMeal(meal: MealEntity)
}