package com.example.salubris.database

import androidx.room.TypeConverter
import com.example.salubris.database.entities.MealComponent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromMealComponentList(value: List<MealComponent>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toMealComponentList(value: String): List<MealComponent> {
        val listType = object : TypeToken<List<MealComponent>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
