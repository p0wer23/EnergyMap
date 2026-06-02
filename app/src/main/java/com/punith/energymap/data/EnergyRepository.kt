package com.punith.energymap.data

import kotlinx.coroutines.flow.Flow

class EnergyRepository(
    private val energyDao: EnergyDao,
    private val activityDao: ActivityDao,
) {
    fun observeEnergyEntries(): Flow<List<EnergyEntry>> = energyDao.observeEntries()

    fun observeActivityEntries(): Flow<List<ActivityEntry>> = activityDao.observeEntries()

    suspend fun addEnergyEntry(entry: EnergyEntry) {
        energyDao.insert(entry)
    }

    suspend fun addActivityEntry(entry: ActivityEntry) {
        activityDao.insert(entry)
    }
}
