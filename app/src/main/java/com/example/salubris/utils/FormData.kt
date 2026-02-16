package com.example.salubris.utils

enum class FieldType {
    STRING, NUMBER, BOOLEAN
}


data class FormData<T>(
    val name: String,
    val type: FieldType,
    var value: T?,
    val required: Boolean = false,
){


    fun hasValue(): Boolean {
        return when (value) {
            is String -> (value as String).isNotBlank()
            is Number -> (value as Number).toDouble() != 0.0
            is Boolean -> true // Boolean always has a value (true/false)
            else -> value != null
        }
    }

}
