package com.punith.energymap.ui

import com.punith.energymap.data.EnergyEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class EnergyDaySection(
    val dateLabel: String,
    val entries: List<EnergyEntry>,
)

sealed interface EnergyEditorMode {
    data object Add : EnergyEditorMode

    data class Edit(val entryId: Long) : EnergyEditorMode
}

data class EnergyEditorState(
    val isVisible: Boolean = false,
    val mode: EnergyEditorMode = EnergyEditorMode.Add,
    val level: Int = 5,
    val note: String = "",
    val timestampText: String? = null,
)

data class EnergyMapUiState(
    val currentEnergy: EnergyEntry? = null,
    val latestOverallEntry: EnergyEntry? = null,
    val hasCheckInToday: Boolean = false,
    val todayEntries: List<EnergyEntry> = emptyList(),
    val previousDaySections: List<EnergyDaySection> = emptyList(),
    val editorState: EnergyEditorState = EnergyEditorState(),
    val pendingDeleteEntry: EnergyEntry? = null,
)

data class EnergyDerivedState(
    val currentEnergy: EnergyEntry?,
    val latestOverallEntry: EnergyEntry?,
    val todayEntries: List<EnergyEntry>,
    val previousDaySections: List<EnergyDaySection>,
)

object EnergyMapTestTags {
    const val ADD_CHECK_IN_BUTTON = "add_check_in_button"
    const val ENERGY_EDITOR_DIALOG = "energy_editor_dialog"
    const val ENERGY_SLIDER = "energy_slider"
    const val ENERGY_NOTE_FIELD = "energy_note_field"
    const val SAVE_CHECK_IN_BUTTON = "save_check_in_button"
    const val DELETE_CHECK_IN_BUTTON = "delete_check_in_button"
    const val DELETE_CONFIRM_BUTTON = "delete_confirm_button"
    const val ENERGY_ENTRY_PREFIX = "energy_entry_"
}

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

private val dayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d, h:mm a", Locale.getDefault())

fun energyBucketLabel(level: Int): String =
    when (level) {
        in 1..2 -> "exhausted"
        in 3..4 -> "low"
        in 5..6 -> "neutral"
        in 7..8 -> "good"
        else -> "high"
    }

fun deriveEnergyState(
    entries: List<EnergyEntry>,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): EnergyDerivedState {
    val sortedEntries = entries.sortedByDescending(EnergyEntry::timestamp)
    val today = localDateAt(nowMillis, zoneId)
    val todayEntries = sortedEntries.filter { localDateAt(it.timestamp, zoneId) == today }
    val previousDaySections = sortedEntries
        .filterNot { localDateAt(it.timestamp, zoneId) == today }
        .groupBy { localDateAt(it.timestamp, zoneId) }
        .entries
        .sortedByDescending { it.key }
        .map { (date, dayEntries) ->
            EnergyDaySection(
                dateLabel = formatDayLabel(date),
                entries = dayEntries,
            )
        }

    return EnergyDerivedState(
        currentEnergy = todayEntries.firstOrNull(),
        latestOverallEntry = sortedEntries.firstOrNull(),
        todayEntries = todayEntries,
        previousDaySections = previousDaySections,
    )
}

fun buildUpdatedEnergyEntry(
    existing: EnergyEntry,
    newLevel: Int,
    newNote: String,
): EnergyEntry = existing.copy(energyLevel = newLevel, note = newNote)

fun formatTime(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).format(timeFormatter)

fun formatDateTime(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).format(dateTimeFormatter)

fun formatLastRecordedText(
    timestamp: Long,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val entryDate = localDateAt(timestamp, zoneId)
    val today = localDateAt(nowMillis, zoneId)
    val dayText = when (entryDate) {
        today.minusDays(1) -> "yesterday"
        else -> formatDayLabel(entryDate)
    }
    return "Last recorded $dayText at ${formatTime(timestamp, zoneId)}"
}

private fun localDateAt(timestamp: Long, zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()

private fun formatDayLabel(date: LocalDate): String = date.format(dayFormatter)
