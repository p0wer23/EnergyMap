package com.punith.energymap.ui

import com.punith.energymap.data.EnergyEntry
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertEquals(listOf(yesterdayEntry), state.previousEntries)
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
        assertEquals(listOf(justBeforeMidnight), state.previousEntries)
    }

    @Test
    fun previousEntriesAreSortedNewestFirst() {
        val today = EnergyEntry(id = 1, timestamp = nowMillis, energyLevel = 5, note = "")
        val yesterday = EnergyEntry(id = 2, timestamp = nowMillis - 86_400_000, energyLevel = 6, note = "")
        val twoDaysAgo = EnergyEntry(id = 3, timestamp = nowMillis - (2 * 86_400_000), energyLevel = 7, note = "")

        val state = deriveEnergyState(
            entries = listOf(twoDaysAgo, yesterday, today),
            nowMillis = nowMillis,
            zoneId = zoneId,
        )

        assertEquals(listOf(yesterday, twoDaysAgo), state.previousEntries)
    }

    @Test
    fun truncatedNotePreviewLeavesFifteenCharactersIntact() {
        val note = "123456789012345"

        assertFalse(isExpandableNote(note))
        assertEquals(note, truncatedNotePreview(note))
    }

    @Test
    fun truncatedNotePreviewCutsOffAfterFifteenCharacters() {
        val note = "1234567890123456"

        assertTrue(isExpandableNote(note))
        assertEquals("123456789012345...", truncatedNotePreview(note))
    }

    @Test
    fun isExpandableNoteUsesTrimmedCharacterLength() {
        val note = "   hello there world   "

        assertTrue(isExpandableNote(note))
        assertEquals("hello there wor...", truncatedNotePreview(note))
    }

    @Test
    fun blankNoteIsNotExpandable() {
        val note = "   "

        assertFalse(isExpandableNote(note))
        assertEquals("", truncatedNotePreview(note))
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
    fun defaultNewEnergyLevelUsesLatestOverallScore() {
        val latestOverallEntry = EnergyEntry(
            id = 1,
            timestamp = nowMillis - 86_400_000,
            energyLevel = 3,
            note = "Yesterday",
        )

        assertEquals(3, defaultNewEnergyLevel(latestOverallEntry))
        assertEquals(5, defaultNewEnergyLevel(null))
    }
}
