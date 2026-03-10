package com.example.salubris.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.salubris.database.entities.Macro
import com.example.salubris.database.entities.Product

data class MacroWithProduct(
    @Embedded
    val macro: Macro,

    @Relation(
        parentColumn = "productId", // the column in Macro
        entityColumn = "uid"         // the primary key in Product
    )
    val product: Product          // full product object
)