package com.example.salubris.database.repositories

import com.example.salubris.database.DAO.TrackedMealDao
import com.example.salubris.database.entities.TrackedMeal
import com.example.salubris.database.relations.TrackedMealWithMeal

class TrackedMealRepository(
    private val dao: TrackedMealDao
) {
    suspend fun addTrackedMeal(mealId: Int, consumedGrams: Float, date: Long) {
        dao.insert(TrackedMeal(mealId = mealId, consumedGrams = consumedGrams, date = date))
    }

    suspend fun getTrackedMealsForDay(dayStart: Long): List<TrackedMeal> {
        return dao.getTrackedMealsForDay(dayStart)
    }

    suspend fun deleteTrackedMeal(trackedMeal: TrackedMeal) {
        dao.delete(trackedMeal)
    }
}