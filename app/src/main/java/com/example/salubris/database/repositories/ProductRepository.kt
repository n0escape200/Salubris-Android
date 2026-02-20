package com.example.salubris.database.repositories;


import com.example.salubris.database.DAO.ProductDao;
import com.example.salubris.database.entities.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    // CREATE single product
    suspend fun insertProduct(product: Product) {
        productDao.insert(product)
    }

    // CREATE multiple products
    suspend fun insertProducts(products: List<Product>) {
        productDao.insertAll(products)
    }

    // READ all products as Flow
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getProducts()
    }

    // READ single product by id as Flow
    fun getProductFlowById(id: Int): Flow<Product?> {
        return productDao.getProductByIdFlow(id)
    }

    // READ single product by id as suspend
    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }

    // UPDATE product
    suspend fun updateProduct(product: Product) {
        productDao.update(product)
    }

    // DELETE product
    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
    }

    // DELETE all products
    suspend fun deleteAllProducts() {
        productDao.deleteAll()
    }
}
