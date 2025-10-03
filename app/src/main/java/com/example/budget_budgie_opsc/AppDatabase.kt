package com.example.budget_budgie_opsc

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Increment version to 3 (Category now includes userId + added Expense table)
@Database(
    entities = [
        User::class,
        Account::class,
        Category::class,
        Expense::class
    ],
    version = 4,
    exportSchema = true // helps track migrations if you add them later
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao

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
                    .fallbackToDestructiveMigration() // clears data on schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}