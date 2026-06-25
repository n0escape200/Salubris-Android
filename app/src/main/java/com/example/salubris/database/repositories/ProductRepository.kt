package com.example.salubris.database.repositories

import com.example.salubris.database.DAO.ProductDao
import com.example.salubris.database.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    suspend fun insertProduct(product: ProductEntity) {
        productDao.insert(product)
    }

    suspend fun insertProducts(products: List<ProductEntity>) {
        productDao.insertAll(products)
    }

    fun getAllProducts(): Flow<List<ProductEntity>> {
        return productDao.getProducts()
    }

    fun getProductFlowById(id: Int): Flow<ProductEntity?> {
        return productDao.getProductByIdFlow(id)
    }

    suspend fun getProductById(id: Int): ProductEntity? {
        return productDao.getProductById(id)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.update(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.delete(product)
    }

    suspend fun deleteAllProducts() {
        productDao.deleteAll()
    }
}
