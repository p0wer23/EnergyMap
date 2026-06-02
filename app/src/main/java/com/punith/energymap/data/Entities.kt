package com.punith.energymap.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EnergyEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val energyLevel: Int,
    val note: String,
)

@Entity
data class ActivityEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startTime: Long,
    val endTime: Long?,
    val note: String,
    val isOngoing: Boolean,
)
