package com.punith.energymap.ui

import com.punith.energymap.data.EnergyEntry
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnergyCheckInModelsTest {
    private val zoneId: ZoneId = ZoneId.of("UTC")
    private val nowMillis: Long = 1_717_329_600_000

    @Test
    fun latestTodayEntryIsSelectedAsCurrentEnergy() {
        val current = EnergyEntry(id = 2, timestamp = nowMillis - 60_000, energyLevel = 8, note = "Latest")
        val olderToday = EnergyEntry(id = 1, timestamp = nowMillis - 3_600_000, energyLevel = 5, note = "")

        val state = deriveEnergyState(
            entries = listOf(olderToday, current),
            nowMillis = nowMillis,
            zoneId = zoneId,
        )

        assertEquals(current, state.currentEnergy)
        assertEquals(listOf(current, olderToday), state.todayEntries)
    }

    @Test
    fun currentEnergyIsNullWhenThereIsNoEntryToday() {
        val yesterdayEntry = EnergyEntry(
            id = 1,
            timestamp = nowMillis - 86_400_000,
            energyLevel = 4,
            note = "Yesterday",
        )

        val state = deriveEnergyState(
            entries = listOf(yesterdayEntry),
            nowMillis = nowMillis,
            zoneId = zoneId,
        )

        assertNull(state.currentEnergy)
        assertEquals(1, state.previousDaySections.size)
    }

    @Test
    fun entriesAreSplitIntoTodayAndPreviousDaysUsingLocalDate() {
        val boundaryNowMillis = 1_717_286_400_000
        val justAfterMidnight = EnergyEntry(id = 1, timestamp = boundaryNowMillis + 60_000, energyLevel = 6, note = "")
        val justBeforeMidnight = EnergyEntry(id = 2, timestamp = boundaryNowMillis - 60_000, energyLevel = 3, note = "")

        val state = deriveEnergyState(
            entries = listOf(justAfterMidnight, justBeforeMidnight),
            nowMillis = boundaryNowMillis + 120_000,
            zoneId = zoneId,
        )

        assertEquals(listOf(justAfterMidnight), state.todayEntries)
        assertEquals(listOf(justBeforeMidnight), state.previousDaySections.single().entries)
    }

    @Test
    fun previousDayGroupsAreSortedNewestFirst() {
        val today = EnergyEntry(id = 1, timestamp = nowMillis, energyLevel = 5, note = "")
        val yesterday = EnergyEntry(id = 2, timestamp = nowMillis - 86_400_000, energyLevel = 6, note = "")
        val twoDaysAgo = EnergyEntry(id = 3, timestamp = nowMillis - (2 * 86_400_000), energyLevel = 7, note = "")

        val state = deriveEnergyState(
            entries = listOf(twoDaysAgo, yesterday, today),
            nowMillis = nowMillis,
            zoneId = zoneId,
        )

        assertEquals(2, state.previousDaySections.size)
        assertEquals(listOf(yesterday), state.previousDaySections[0].entries)
        assertEquals(listOf(twoDaysAgo), state.previousDaySections[1].entries)
    }

    @Test
    fun energyLabelBucketsMatchRequirements() {
        assertEquals("exhausted", energyBucketLabel(1))
        assertEquals("exhausted", energyBucketLabel(2))
        assertEquals("low", energyBucketLabel(3))
        assertEquals("low", energyBucketLabel(4))
        assertEquals("neutral", energyBucketLabel(5))
        assertEquals("neutral", energyBucketLabel(6))
        assertEquals("good", energyBucketLabel(7))
        assertEquals("good", energyBucketLabel(8))
        assertEquals("high", energyBucketLabel(9))
        assertEquals("high", energyBucketLabel(10))
    }

    @Test
    fun editingPreservesOriginalTimestamp() {
        val existing = EnergyEntry(id = 9, timestamp = nowMillis - 10_000, energyLevel = 5, note = "Old")

        val updated = buildUpdatedEnergyEntry(
            existing = existing,
            newLevel = 8,
            newNote = "Updated",
        )

        assertEquals(existing.timestamp, updated.timestamp)
        assertEquals(8, updated.energyLevel)
        assertEquals("Updated", updated.note)
    }

    @Test
    fun deletingEntryRemovesItFromDerivedState() {
        val today = EnergyEntry(id = 1, timestamp = nowMillis, energyLevel = 7, note = "")
        val yesterday = EnergyEntry(id = 2, timestamp = nowMillis - 86_400_000, energyLevel = 4, note = "")

        val state = deriveEnergyState(
            entries = listOf(today, yesterday).filterNot { it.id == today.id },
            nowMillis = nowMillis,
            zoneId = zoneId,
        )

        assertNull(state.currentEnergy)
        assertEquals(listOf(yesterday), state.previousDaySections.single().entries)
    }
}
