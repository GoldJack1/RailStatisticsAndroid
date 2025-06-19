package com.jw.railstatistics.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Station::class, TicketRecord::class], version = 2, exportSchema = false)
@TypeConverters(UsageDataConverter::class, LoyaltyProgramConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rail_statistics_database"
                )
                .fallbackToDestructiveMigration() // For simplicity, recreate database on version change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 