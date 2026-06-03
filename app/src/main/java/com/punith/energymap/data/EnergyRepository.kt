package com.punith.energymap.data

import kotlinx.coroutines.flow.Flow
import kotlin.math.max

enum class ActivitySaveResult {
    Success,
    Overlap,
    NoActiveEntry,
}

class EnergyRepository(
    private val energyDao: EnergyDao,
    private val activityDao: ActivityDao,
) {
    fun observeEnergyEntries(): Flow<List<EnergyEntry>> = energyDao.observeEntries()

    fun observeActivityEntries(): Flow<List<ActivityEntry>> = activityDao.observeEntries()

    suspend fun saveEnergyEntry(entry: EnergyEntry) {
        energyDao.upsert(entry)
    }

    suspend fun deleteEnergyEntry(entry: EnergyEntry) {
        energyDao.delete(entry)
    }

    suspend fun saveActivityEntry(
        entry: ActivityEntry,
        excludeId: Long? = entry.takeIf { it.id != 0L }?.id,
    ): ActivitySaveResult {
        val effectiveEnd = entry.endTime ?: Long.MAX_VALUE
        if (activityDao.hasOverlappingEntry(entry.startTime, effectiveEnd, excludeId)) {
            return ActivitySaveResult.Overlap
        }
        activityDao.upsert(entry)
        return ActivitySaveResult.Success
    }

    suspend fun startActivity(
        title: String,
        note: String,
        now: Long,
    ): ActivitySaveResult = saveActivityEntry(
        ActivityEntry(
            title = title,
            startTime = now,
            endTime = null,
            note = note,
            isOngoing = true,
        ),
        excludeId = null,
    )

    suspend fun endCurrentActivity(now: Long): ActivitySaveResult {
        val currentActivity = activityDao.getCurrentOngoingEntry() ?: return ActivitySaveResult.NoActiveEntry
        val endTime = max(now, currentActivity.startTime + 60_000)
        activityDao.upsert(
            currentActivity.copy(
                endTime = endTime,
                isOngoing = false,
            ),
        )
        return ActivitySaveResult.Success
    }

    suspend fun deleteActivityEntry(entry: ActivityEntry) {
        activityDao.delete(entry)
    }
}
