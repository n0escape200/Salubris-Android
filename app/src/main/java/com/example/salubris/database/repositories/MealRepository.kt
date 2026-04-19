package com.example.salubris.database.repositories

import com.example.salubris.database.DAO.MealDao
import com.example.salubris.database.entities.Meal
import com.example.salubris.database.entities.MealProductCrossRef
import com.example.salubris.database.entities.MealWithProducts
import com.example.salubris.database.entities.Product
import com.example.salubris.database.relations.ProductWithQuantity   // ✅ fixed import

class MealRepository(
    private val mealDao: MealDao
) {
    // These now call the custom @Transaction methods in DAO
    suspend fun getAllMealsWithProducts(): List<MealWithProducts> =
        mealDao.getAllMealsWithProducts()

    suspend fun getMealWithProducts(mealId: Int): MealWithProducts? =
        mealDao.getMealWithProducts(mealId)

    suspend fun getAllProducts(): List<Product> =
        mealDao.getAllProducts()

    suspend fun insertMealWithProducts(
        mealName: String,
        productsWithQuantities: List<ProductWithQuantity>
    ) {
        val mealId = mealDao.insertMeal(Meal(name = mealName)).toInt()
        val crossRefs = productsWithQuantities.map {
            MealProductCrossRef(
                mealId = mealId,
                productId = it.product.uid,
                quantity = it.quantity
            )
        }
        if (crossRefs.isNotEmpty()) {
            mealDao.insertAllCrossRefs(crossRefs)
        }
    }

    suspend fun updateMealProducts(
        mealId: Int,
        productsWithQuantities: List<ProductWithQuantity>
    ) {
        mealDao.deleteCrossRefsForMeal(mealId)
        val crossRefs = productsWithQuantities.map {
            MealProductCrossRef(
                mealId = mealId,
                productId = it.product.uid,
                quantity = it.quantity
            )
        }
        if (crossRefs.isNotEmpty()) {
            mealDao.insertAllCrossRefs(crossRefs)
        }
    }

    suspend fun deleteMeal(meal: Meal) {
        mealDao.deleteCrossRefsForMeal(meal.uid)
        mealDao.deleteMeal(meal)
    }
}