package com.punith.energymap.ui

import androidx.compose.ui.graphics.Color
import com.punith.energymap.data.EnergyEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class EnergyEntryFilter {
    TODAY,
    PREVIOUS,
}

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
    val previousEntries: List<EnergyEntry> = emptyList(),
    val selectedEntryFilter: EnergyEntryFilter = EnergyEntryFilter.TODAY,
    val expandedEntryId: Long? = null,
    val editorState: EnergyEditorState = EnergyEditorState(),
    val pendingDeleteEntry: EnergyEntry? = null,
)

data class EnergyDerivedState(
    val currentEnergy: EnergyEntry?,
    val latestOverallEntry: EnergyEntry?,
    val todayEntries: List<EnergyEntry>,
    val previousEntries: List<EnergyEntry>,
)

object EnergyMapTestTags {
    const val ADD_CHECK_IN_BUTTON = "add_check_in_button"
    const val ENERGY_CHECK_INS_HEADER = "energy_check_ins_header"
    const val ENERGY_CHECK_INS_HEADER_ACTION = "energy_check_ins_header_action"
    const val LATEST_TODAY_ENTRY = "latest_today_entry"
    const val ENERGY_EDITOR_DIALOG = "energy_editor_dialog"
    const val ENERGY_SLIDER = "energy_slider"
    const val ENERGY_NOTE_FIELD = "energy_note_field"
    const val SAVE_CHECK_IN_BUTTON = "save_check_in_button"
    const val DELETE_CHECK_IN_BUTTON = "delete_check_in_button"
    const val DELETE_CONFIRM_BUTTON = "delete_confirm_button"
    const val TODAY_FILTER_BUTTON = "today_filter_button"
    const val PREVIOUS_FILTER_BUTTON = "previous_filter_button"
    const val ENERGY_ENTRY_PREFIX = "energy_entry_"
    const val ENERGY_ENTRY_NOTE_PREFIX = "energy_entry_note_"
    const val ENERGY_ENTRY_EDIT_PREFIX = "energy_entry_edit_"
}

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d, h:mm a", Locale.getDefault())

fun deriveEnergyState(
    entries: List<EnergyEntry>,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): EnergyDerivedState {
    val sortedEntries = entries.sortedByDescending(EnergyEntry::timestamp)
    val today = localDateAt(nowMillis, zoneId)
    val todayEntries = sortedEntries.filter { localDateAt(it.timestamp, zoneId) == today }
    val previousEntries = sortedEntries.filterNot { localDateAt(it.timestamp, zoneId) == today }

    return EnergyDerivedState(
        currentEnergy = todayEntries.firstOrNull(),
        latestOverallEntry = sortedEntries.firstOrNull(),
        todayEntries = todayEntries,
        previousEntries = previousEntries,
    )
}

fun buildUpdatedEnergyEntry(
    existing: EnergyEntry,
    newLevel: Int,
    newNote: String,
): EnergyEntry = existing.copy(energyLevel = newLevel, note = newNote)

fun defaultNewEnergyLevel(latestOverallEntry: EnergyEntry?): Int =
    latestOverallEntry?.energyLevel ?: 5

fun formatTime(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).format(timeFormatter)

fun formatDateTime(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).format(dateTimeFormatter)

private fun localDateAt(timestamp: Long, zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()

fun energyScoreColor(level: Int): Color {
    val clampedLevel = level.coerceIn(1, 10)
    val fraction = (clampedLevel - 1) / 9f
    val red = 0xFFB3261E
    val green = 0xFF4CAF50
    return lerpColor(
        start = Color(red),
        end = Color(green),
        fraction = fraction,
    )
}

fun isExpandableNote(note: String): Boolean = note.trim().length > NOTE_PREVIEW_CHARACTER_LIMIT

fun truncatedNotePreview(note: String): String {
    val trimmedNote = note.trim()
    return if (trimmedNote.length <= NOTE_PREVIEW_CHARACTER_LIMIT) {
        trimmedNote
    } else {
        trimmedNote.take(NOTE_PREVIEW_CHARACTER_LIMIT) + "..."
    }
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color =
    Color(
        red = start.red + ((end.red - start.red) * fraction),
        green = start.green + ((end.green - start.green) * fraction),
        blue = start.blue + ((end.blue - start.blue) * fraction),
        alpha = start.alpha + ((end.alpha - start.alpha) * fraction),
    )

private const val NOTE_PREVIEW_CHARACTER_LIMIT = 15
