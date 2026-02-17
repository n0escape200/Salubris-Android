package com.example.salubris.database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.salubris.database.entities.Product
import kotlinx.coroutines.flow.Flow


@Dao
interface ProductDao {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    // READ ALL (Flow for live updates)
    @Query("SELECT * FROM Product ORDER BY name ASC")
    fun getProducts(): Flow<List<Product>>

    // READ single item as Flow
    @Query("SELECT * FROM Product WHERE uid = :id")
    fun getProductByIdFlow(id: Int): Flow<Product?>

    // READ single item as suspend
    @Query("SELECT * FROM Product WHERE uid = :id")
    suspend fun getProductById(id: Int): Product?

    // UPDATE
    @Update
    suspend fun update(product: Product)

    // DELETE
    @Delete
    suspend fun delete(product: Product)

    @Query("DELETE FROM Product")
    suspend fun deleteAll()
}