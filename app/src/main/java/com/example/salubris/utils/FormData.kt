package com.example.salubris.utils

enum class FieldType {
    STRING, NUMBER, BOOLEAN
}


data class FormData(
    var name: String,
    var type: FieldType,
    var value: Any,
    var required: Boolean = false,
){


    fun hasValue(): Boolean {
        return when (value) {
            is String -> (value as String).isNotBlank()
            is Number -> (value as Number).toDouble() != 0.0
            is Boolean -> true // Boolean always has a value (true/false)
            else -> true
        }
    }

}
