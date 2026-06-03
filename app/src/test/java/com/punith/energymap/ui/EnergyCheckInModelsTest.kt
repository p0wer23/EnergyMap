package com.punith.energymap.ui

import com.punith.energymap.data.ActivityEntry
import com.punith.energymap.data.EnergyEntry
import java.time.LocalDate
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

    @Test
    fun overlapDetectionRejectsExactOverlaps() {
        assertTrue(
            activityIntervalsOverlap(
                existingStart = 1_000L,
                existingEnd = 2_000L,
                candidateStart = 1_000L,
                candidateEnd = 2_000L,
            ),
        )
    }

    @Test
    fun adjacentActivitiesAreAllowed() {
        assertFalse(
            activityIntervalsOverlap(
                existingStart = 1_000L,
                existingEnd = 2_000L,
                candidateStart = 2_000L,
                candidateEnd = 3_000L,
            ),
        )
    }

    @Test
    fun ongoingActivityBlocksFutureActivity() {
        assertTrue(
            activityIntervalsOverlap(
                existingStart = 1_000L,
                existingEnd = null,
                candidateStart = 2_000L,
                candidateEnd = 3_000L,
            ),
        )
    }

    @Test
    fun crossMidnightActivityIsClippedAcrossDays() {
        val selectedDate = LocalDate.of(2024, 6, 1)
        val start = selectedDate.atTime(23, 0).atZone(zoneId).toInstant().toEpochMilli()
        val end = selectedDate.plusDays(1).atTime(1, 0).atZone(zoneId).toInstant().toEpochMilli()
        val activity = ActivityEntry(
            id = 1,
            title = "Late work",
            startTime = start,
            endTime = end,
            note = "",
            isOngoing = false,
        )

        val firstDayState = deriveActivityTimelineState(
            activityEntries = listOf(activity),
            energyEntries = emptyList(),
            selectedDate = selectedDate,
            nowMillis = end,
            zoneId = zoneId,
        )
        val secondDayState = deriveActivityTimelineState(
            activityEntries = listOf(activity),
            energyEntries = emptyList(),
            selectedDate = selectedDate.plusDays(1),
            nowMillis = end,
            zoneId = zoneId,
        )

        val firstDayBlock = firstDayState.timelineItems.filterIsInstance<DailyTimelineItem.ActivityBlock>().single()
        val secondDayBlock = secondDayState.timelineItems.filterIsInstance<DailyTimelineItem.ActivityBlock>().single()

        assertTrue(firstDayBlock.topFraction > 0.95f)
        assertTrue(firstDayBlock.heightFraction > 0f)
        assertEquals(0f, secondDayBlock.topFraction)
        assertTrue(secondDayBlock.heightFraction > 0f)
    }

    @Test
    fun energyMarkersUseSelectedDayPosition() {
        val selectedDate = LocalDate.of(2024, 6, 1)
        val timestamp = selectedDate.atTime(6, 0).atZone(zoneId).toInstant().toEpochMilli()
        val energy = EnergyEntry(id = 1, timestamp = timestamp, energyLevel = 7, note = "")

        val state = deriveActivityTimelineState(
            activityEntries = emptyList(),
            energyEntries = listOf(energy),
            selectedDate = selectedDate,
            nowMillis = timestamp,
            zoneId = zoneId,
        )

        val marker = state.timelineItems.filterIsInstance<DailyTimelineItem.EnergyMarker>().single()
        assertEquals(0.25f, marker.topFraction, 0.0001f)
    }

    @Test
    fun timelineItemsSortByActivityTimeAndIncludeEnergyMarkers() {
        val selectedDate = LocalDate.of(2024, 6, 1)
        val earlyStart = selectedDate.atTime(8, 0).atZone(zoneId).toInstant().toEpochMilli()
        val lateStart = selectedDate.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()
        val energyTime = selectedDate.atTime(9, 0).atZone(zoneId).toInstant().toEpochMilli()
        val entries = listOf(
            ActivityEntry(id = 2, title = "Late", startTime = lateStart, endTime = lateStart + 3_600_000, note = "", isOngoing = false),
            ActivityEntry(id = 1, title = "Early", startTime = earlyStart, endTime = earlyStart + 3_600_000, note = "", isOngoing = false),
        )
        val energy = EnergyEntry(id = 9, timestamp = energyTime, energyLevel = 5, note = "")

        val state = deriveActivityTimelineState(
            activityEntries = entries,
            energyEntries = listOf(energy),
            selectedDate = selectedDate,
            nowMillis = lateStart,
            zoneId = zoneId,
        )

        val blocks = state.timelineItems.filterIsInstance<DailyTimelineItem.ActivityBlock>()
        assertEquals(listOf(1L, 2L), blocks.map { it.entry.id })
        assertEquals(1, state.timelineItems.filterIsInstance<DailyTimelineItem.EnergyMarker>().size)
    }
}
