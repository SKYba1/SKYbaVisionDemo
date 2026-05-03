package com.skyba.vision.demo.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


/**
 * [Data Layer] The main database configuration for the application.
 *
 * This class serves as the primary access point to the persisted SQLite data.
 * It uses the Singleton pattern to prevent multiple instances of the database
 * opening simultaneously, which could lead to memory leaks or data corruption.
 */
@Database(entities = [TimerSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timerDao(): TimerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timer_stats_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}