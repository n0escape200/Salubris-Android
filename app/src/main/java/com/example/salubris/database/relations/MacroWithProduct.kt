package com.example.salubris.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.salubris.database.entities.Macro
import com.example.salubris.database.entities.Product

data class MacroWithProduct(
    @Embedded
    val macro: Macro,
    @Relation(
        parentColumn = "productId",
        entityColumn = "uid"
    )
    val product: Product?  // This will hold the actual Product object
)