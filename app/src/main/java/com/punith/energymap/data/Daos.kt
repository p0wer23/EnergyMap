package com.punith.energymap.data

import androidx.room.Delete
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EnergyDao {
    @Query("SELECT * FROM EnergyEntry ORDER BY timestamp DESC")
    fun observeEntries(): Flow<List<EnergyEntry>>

    @Upsert
    suspend fun upsert(entry: EnergyEntry)

    @Delete
    suspend fun delete(entry: EnergyEntry)
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM ActivityEntry ORDER BY startTime DESC")
    fun observeEntries(): Flow<List<ActivityEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ActivityEntry)
}
