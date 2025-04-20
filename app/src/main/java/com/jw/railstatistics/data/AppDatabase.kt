package com.jw.railstatistics.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Station::class], version = 1, exportSchema = false)
@TypeConverters(UsageDataConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rail_statistics_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 