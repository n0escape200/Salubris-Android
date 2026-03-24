package com.example.salubris.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.salubris.database.entities.Macro
import com.example.salubris.database.entities.Product
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId

fun Float.truncate2Decimals(): Float {
    return (this * 100).toInt() / 100f
}

@RequiresApi(Build.VERSION_CODES.O)
fun getStartOfDay(timestamp: Long): Long {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun calculateMacrosForProduct(product: Product, quantity: Float): Map<String, String> {
    return mapOf(
        "name" to product.name,
        "calories" to ((product.calories / 100) * quantity).toString(),
        "protein" to ((product.protein / 100) * quantity).toString(),
        "carbs" to ((product.carbs / 100) * quantity).toString(),
        "fats" to ((product.fats / 100) * quantity).toString(),
    )
}