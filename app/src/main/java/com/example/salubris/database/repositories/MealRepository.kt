package com.example.salubris.database.repositories

import com.example.salubris.database.DAO.MealDao
import com.example.salubris.database.entities.MealEntity

class MealRepository(
    private val mealDao: MealDao
) {
    suspend fun insertMeal(meal: MealEntity): Long = mealDao.insertMeal(meal)
    suspend fun getAllMeals(): List<MealEntity> = mealDao.getAllMeals()
    suspend fun getMealById(id: Int): MealEntity? = mealDao.getMealById(id)
    suspend fun updateMeal(meal: MealEntity) = mealDao.updateMeal(meal)  // ✅ new
    suspend fun deleteMeal(meal: MealEntity) = mealDao.deleteMeal(meal)
}