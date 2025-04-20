package com.jw.railstatistics.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.util.concurrent.atomic.AtomicLong

@Entity(tableName = "stations")
data class Station(
    @PrimaryKey(autoGenerate = true)
    val id: Long = nextId(),
    val name: String,
    val country: String,
    val county: String,
    val trainOperator: String,
    val visitStatus: String,
    val visitedDate: String,
    val favorite: Boolean = false,
    val latitude: String,
    val longitude: String,
    @TypeConverters(UsageDataConverter::class)
    val yearlyUsage: Map<Int, String> = emptyMap()
) : Serializable {
    companion object {
        private val idCounter = AtomicLong(System.currentTimeMillis())
        
        fun nextId(): Long = idCounter.incrementAndGet()
    }
}

class UsageDataConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): Map<Int, String> {
        val mapType = object : TypeToken<Map<Int, String>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<Int, String>): String {
        return gson.toJson(map)
    }
} 