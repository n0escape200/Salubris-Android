package com.example.salubris.utils

fun Float.truncate2Decimals(): Float {
    return (this * 100).toInt() / 100f
}