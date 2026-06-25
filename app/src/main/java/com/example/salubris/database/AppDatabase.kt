package com.example.salubris.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.salubris.database.DAO.MacroDao
import com.example.salubris.database.DAO.MealDao
import com.example.salubris.database.DAO.ProductDao
import com.example.salubris.database.DAO.SettingDao
import com.example.salubris.database.DAO.WaterDao
import com.example.salubris.database.dao.StepHistoryDao
import com.example.salubris.database.entities.DailyWaterHistoryEntity
import com.example.salubris.database.entities.MacroEntity
import com.example.salubris.database.entities.MealEntity
import com.example.salubris.database.entities.ProductEntity
import com.example.salubris.database.entities.SettingEntity
import com.example.salubris.database.entities.StepHistoryEntity
import com.example.salubris.database.entities.WaterEntity

@Database(
    entities = [
        ProductEntity::class,
        SettingEntity::class,
        MacroEntity::class,
        WaterEntity::class,
        DailyWaterHistoryEntity::class,
        MealEntity::class,
        StepHistoryEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun settingDao(): SettingDao
    abstract fun macroDao(): MacroDao
    abstract fun waterDao(): WaterDao
    abstract fun mealDao(): MealDao
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
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
