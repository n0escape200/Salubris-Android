package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.salubris.database.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE uid = :id")
    fun getProductByIdFlow(id: Int): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE uid = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
