package com.example.salubris.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val _favorites = MutableStateFlow(loadFavorites())
    val favorites: StateFlow<List<String>> = _favorites

    private fun loadFavorites(): List<String> {
        val default = listOf("Home", "Tracking", "Products", "Settings")
        val json = prefs.getString("favorites", null)
        return if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                com.google.gson.Gson().fromJson(json, type)
            } catch (e: Exception) {
                default
            }
        } else {
            default
        }
    }

    fun saveFavorites(newFavorites: List<String>) {
        val json = com.google.gson.Gson().toJson(newFavorites)
        prefs.edit().putString("favorites", json).apply()
        _favorites.value = newFavorites
    }
}