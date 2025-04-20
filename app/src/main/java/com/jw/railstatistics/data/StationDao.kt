package com.jw.railstatistics.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {
    @Query("SELECT * FROM stations")
    fun getAllStations(): Flow<List<Station>>

    @Query("SELECT * FROM stations WHERE id = :id")
    suspend fun getStationById(id: Long): Station?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: Station): Long

    @Update
    suspend fun update(station: Station)

    @Delete
    suspend fun delete(station: Station)

    @Query("SELECT * FROM stations WHERE favorite = 1")
    fun getFavoriteStations(): Flow<List<Station>>
} 