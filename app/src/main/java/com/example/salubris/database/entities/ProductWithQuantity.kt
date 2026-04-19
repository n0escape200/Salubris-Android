package com.example.salubris.database.relations

import androidx.room.Embedded
import com.example.salubris.database.entities.Product

data class ProductWithQuantity(
    @Embedded
    val product: Product,
    val quantity: Float
)