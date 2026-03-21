package com.example.salubris.utils

import android.os.Build
import androidx.annotation.RequiresApi
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