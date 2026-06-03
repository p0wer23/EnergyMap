package com.punith.energymap.data

import androidx.room.Delete
import androidx.room.Dao
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

    @Query("SELECT * FROM ActivityEntry WHERE isOngoing = 1 ORDER BY startTime DESC LIMIT 1")
    suspend fun getCurrentOngoingEntry(): ActivityEntry?

    @Query(
        """
        SELECT COUNT(*) > 0
        FROM ActivityEntry
        WHERE (:excludeId IS NULL OR id != :excludeId)
            AND startTime < :newEnd
            AND COALESCE(endTime, 9223372036854775807) > :newStart
        """,
    )
    suspend fun hasOverlappingEntry(
        newStart: Long,
        newEnd: Long,
        excludeId: Long? = null,
    ): Boolean

    @Upsert
    suspend fun upsert(entry: ActivityEntry)

    @Delete
    suspend fun delete(entry: ActivityEntry)
}
