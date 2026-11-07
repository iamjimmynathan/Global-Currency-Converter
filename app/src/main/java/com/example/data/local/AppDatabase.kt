package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ConversionHistoryEntity
import com.example.data.model.CurrencyRateEntity
import com.example.data.model.FavoriteCurrencyEntity
import com.example.data.model.SelectedCurrencyEntity

@Database(
    entities = [
        CurrencyRateEntity::class,
        FavoriteCurrencyEntity::class,
        SelectedCurrencyEntity::class,
        ConversionHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun currencyDao(): CurrencyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "currency_converter.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
