package com.example.salubris.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.salubris.database.DAO.MacroLineDao
import com.example.salubris.database.DAO.ProductDao
import com.example.salubris.database.DAO.SettingDao
import com.example.salubris.database.entities.Macro
import com.example.salubris.database.entities.Product
import com.example.salubris.database.entities.Setting

@Database(entities = [Product::class, Setting::class, Macro::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun settingDao(): SettingDao
    abstract fun macroDao(): MacroLineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Access the database from anywhere
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Salubris"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
