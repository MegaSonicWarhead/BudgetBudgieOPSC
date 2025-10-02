package com.example.budget_budgie_opsc

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Incremented version to 2 to match schema changes
@Database(entities = [User::class, Account::class, Category::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_app_db"
                )
                    .fallbackToDestructiveMigration() // clears old DB if version changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}