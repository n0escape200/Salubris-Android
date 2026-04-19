package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.salubris.database.entities.Meal
import com.example.salubris.database.entities.MealProductCrossRef
import com.example.salubris.database.entities.MealWithProducts
import com.example.salubris.database.entities.Product
import com.example.salubris.database.relations.ProductWithQuantity

@Dao
interface MealDao {

    @Insert
    suspend fun insertMeal(meal: Meal): Long

    @Insert
    suspend fun insertCrossRef(crossRef: MealProductCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCrossRefs(crossRefs: List<MealProductCrossRef>)

    @Query("SELECT * FROM Meal ORDER BY uid DESC")
    suspend fun getAllMeals(): List<Meal>

    @Query("SELECT * FROM Meal WHERE uid = :mealId")
    suspend fun getMeal(mealId: Int): Meal?

    @Query("""
        SELECT 
            Product.*, 
            MealProductCrossRef.quantity 
        FROM Product 
        INNER JOIN MealProductCrossRef ON Product.uid = MealProductCrossRef.productId 
        WHERE MealProductCrossRef.mealId = :mealId
    """)
    suspend fun getProductsWithQuantityForMeal(mealId: Int): List<ProductWithQuantity>

    @Transaction
    suspend fun getAllMealsWithProducts(): List<MealWithProducts> {
        val meals = getAllMeals()
        return meals.map { meal ->
            MealWithProducts(
                meal = meal,
                products = getProductsWithQuantityForMeal(meal.uid)
            )
        }
    }

    @Transaction
    suspend fun getMealWithProducts(mealId: Int): MealWithProducts? {
        val meal = getMeal(mealId) ?: return null
        return MealWithProducts(
            meal = meal,
            products = getProductsWithQuantityForMeal(mealId)
        )
    }

    @Delete
    suspend fun deleteMeal(meal: Meal)

    @Query("DELETE FROM MealProductCrossRef WHERE mealId = :mealId")
    suspend fun deleteCrossRefsForMeal(mealId: Int)

    @Query("SELECT * FROM Product ORDER BY name")
    suspend fun getAllProducts(): List<Product>
}