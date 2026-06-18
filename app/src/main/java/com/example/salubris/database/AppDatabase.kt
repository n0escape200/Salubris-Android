package com.example.salubris.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.salubris.database.DAO.MacroLineDao
import com.example.salubris.database.DAO.MealDao
import com.example.salubris.database.DAO.ProductDao
import com.example.salubris.database.DAO.SettingDao
import com.example.salubris.database.DAO.TrackedMealDao
import com.example.salubris.database.DAO.WaterDao
import com.example.salubris.database.dao.StepHistoryDao
import com.example.salubris.database.entities.DailyWaterHistory
import com.example.salubris.database.entities.Macro
import com.example.salubris.database.entities.Meal
import com.example.salubris.database.entities.MealProductCrossRef
import com.example.salubris.database.entities.Product
import com.example.salubris.database.entities.Setting
import com.example.salubris.database.entities.StepHistoryEntity
import com.example.salubris.database.entities.TrackedMeal
import com.example.salubris.database.entities.WaterEntry

@Database(
    entities = [
        Product::class,
        Setting::class,
        Macro::class,
        WaterEntry::class,
        DailyWaterHistory::class,
        Meal::class,
        MealProductCrossRef::class,
        TrackedMeal::class,
        StepHistoryEntity::class
    ],
    version = 2   // increment version because you added tables
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun settingDao(): SettingDao
    abstract fun macroDao(): MacroLineDao
    abstract fun waterDao(): WaterDao
    abstract fun mealDao(): MealDao
    abstract fun trackedMealDao(): TrackedMealDao

    abstract fun stepHistoryDao(): StepHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

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