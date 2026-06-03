package com.punith.energymap.ui

import androidx.compose.ui.graphics.Color
import com.punith.energymap.data.ActivityEntry
import com.punith.energymap.data.EnergyEntry
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

enum class EnergyMapView {
    CheckIns,
    ActivityTimeline,
}

enum class EnergyEntryFilter {
    TODAY,
    PREVIOUS,
}

sealed interface EnergyEditorMode {
    data object Add : EnergyEditorMode

    data class Edit(val entryId: Long) : EnergyEditorMode
}

sealed interface ActivityEditorMode {
    data object AddManual : ActivityEditorMode

    data class Edit(val entryId: Long) : ActivityEditorMode
}

enum class ActivityTimeField {
    Start,
    End,
}

enum class ActivityBlockContentMode {
    Full,
    Compact,
    TimeOnly,
    None,
}

data class EnergyEditorState(
    val isVisible: Boolean = false,
    val mode: EnergyEditorMode = EnergyEditorMode.Add,
    val level: Int = 5,
    val note: String = "",
    val timestampText: String? = null,
)

data class ActivityEditorState(
    val isVisible: Boolean = false,
    val mode: ActivityEditorMode = ActivityEditorMode.AddManual,
    val title: String = "",
    val note: String = "",
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(10, 0),
    val endsNextDay: Boolean = false,
    val validationMessage: String? = null,
    val timePickerField: ActivityTimeField? = null,
)

data class QuickStartActivityState(
    val isVisible: Boolean = false,
    val title: String = "",
    val note: String = "",
    val validationMessage: String? = null,
)

sealed interface DailyTimelineItem {
    data class ActivityBlock(
        val entry: ActivityEntry,
        val topFraction: Float,
        val heightFraction: Float,
        val title: String,
        val notePreview: String?,
        val timeRangeText: String,
        val isOngoing: Boolean,
        val contentMode: ActivityBlockContentMode,
    ) : DailyTimelineItem

    data class EnergyMarker(
        val entry: EnergyEntry,
        val topFraction: Float,
        val scoreColor: Color,
    ) : DailyTimelineItem
}

data class ActivityTimelineDerivedState(
    val currentActivity: ActivityEntry?,
    val timelineItems: List<DailyTimelineItem>,
)

data class EnergyMapUiState(
    val currentView: EnergyMapView = EnergyMapView.CheckIns,
    val currentEnergy: EnergyEntry? = null,
    val latestOverallEntry: EnergyEntry? = null,
    val hasCheckInToday: Boolean = false,
    val todayEntries: List<EnergyEntry> = emptyList(),
    val previousEntries: List<EnergyEntry> = emptyList(),
    val selectedEntryFilter: EnergyEntryFilter = EnergyEntryFilter.TODAY,
    val expandedEntryId: Long? = null,
    val editorState: EnergyEditorState = EnergyEditorState(),
    val pendingDeleteEntry: EnergyEntry? = null,
    val selectedActivityDate: LocalDate = LocalDate.now(),
    val activityEntries: List<ActivityEntry> = emptyList(),
    val currentActivity: ActivityEntry? = null,
    val dailyTimelineItems: List<DailyTimelineItem> = emptyList(),
    val activityEditorState: ActivityEditorState = ActivityEditorState(),
    val quickStartActivityState: QuickStartActivityState = QuickStartActivityState(),
    val pendingDeleteActivityEntry: ActivityEntry? = null,
    val activityValidationMessage: String? = null,
)

data class EnergyDerivedState(
    val currentEnergy: EnergyEntry?,
    val latestOverallEntry: EnergyEntry?,
    val todayEntries: List<EnergyEntry>,
    val previousEntries: List<EnergyEntry>,
)

object EnergyMapTestTags {
    const val ADD_CHECK_IN_BUTTON = "add_check_in_button"
    const val ACTIVITY_VIEW_BUTTON = "activity_view_button"
    const val BACK_TO_CHECK_INS_BUTTON = "back_to_check_ins_button"
    const val ENERGY_CHECK_INS_HEADER = "energy_check_ins_header"
    const val ENERGY_CHECK_INS_HEADER_ACTION = "energy_check_ins_header_action"
    const val ACTIVITY_TIMELINE_SCREEN = "activity_timeline_screen"
    const val SELECTED_ACTIVITY_DATE = "selected_activity_date"
    const val PREVIOUS_DAY_BUTTON = "previous_day_button"
    const val NEXT_DAY_BUTTON = "next_day_button"
    const val TODAY_ACTIVITY_BUTTON = "today_activity_button"
    const val START_ACTIVITY_BUTTON = "start_activity_button"
    const val END_CURRENT_ACTIVITY_BUTTON = "end_current_activity_button"
    const val MANUAL_ACTIVITY_BUTTON = "manual_activity_button"
    const val ACTIVITY_EDITOR_DIALOG = "activity_editor_dialog"
    const val QUICK_START_DIALOG = "quick_start_dialog"
    const val ACTIVITY_TITLE_FIELD = "activity_title_field"
    const val ACTIVITY_NOTE_FIELD = "activity_note_field"
    const val ACTIVITY_START_TIME_BUTTON = "activity_start_time_button"
    const val ACTIVITY_END_TIME_BUTTON = "activity_end_time_button"
    const val ACTIVITY_ENDS_NEXT_DAY_TOGGLE = "activity_ends_next_day_toggle"
    const val ACTIVITY_SAVE_BUTTON = "activity_save_button"
    const val ACTIVITY_DELETE_BUTTON = "activity_delete_button"
    const val ACTIVITY_DELETE_CONFIRM_BUTTON = "activity_delete_confirm_button"
    const val ACTIVITY_VALIDATION_MESSAGE = "activity_validation_message"
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
    const val ACTIVITY_BLOCK_PREFIX = "activity_block_"
    const val ENERGY_TIMELINE_MARKER_PREFIX = "energy_timeline_marker_"
    const val ACTIVITY_TIME_PICKER_DIALOG = "activity_time_picker_dialog"
}

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d, h:mm a", Locale.getDefault())

private val activityDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

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

fun deriveActivityTimelineState(
    activityEntries: List<ActivityEntry>,
    energyEntries: List<EnergyEntry>,
    selectedDate: LocalDate,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): ActivityTimelineDerivedState {
    val currentActivity = activityEntries
        .filter(ActivityEntry::isOngoing)
        .maxByOrNull(ActivityEntry::startTime)

    val items = buildList {
        activityEntries
            .sortedBy(ActivityEntry::startTime)
            .mapNotNull { entry ->
                buildActivityBlock(
                    entry = entry,
                    selectedDate = selectedDate,
                    nowMillis = nowMillis,
                    zoneId = zoneId,
                )
            }
            .forEach(::add)

        energyEntries
            .sortedBy(EnergyEntry::timestamp)
            .mapNotNull { entry ->
                buildEnergyMarker(
                    entry = entry,
                    selectedDate = selectedDate,
                    zoneId = zoneId,
                )
            }
            .forEach(::add)
    }

    return ActivityTimelineDerivedState(
        currentActivity = currentActivity,
        timelineItems = items,
    )
}

fun buildUpdatedEnergyEntry(
    existing: EnergyEntry,
    newLevel: Int,
    newNote: String,
): EnergyEntry = existing.copy(energyLevel = newLevel, note = newNote)

fun buildUpdatedActivityEntry(
    existing: ActivityEntry,
    title: String,
    note: String,
    startTimeMillis: Long,
    endTimeMillis: Long?,
): ActivityEntry = existing.copy(
    title = title,
    note = note,
    startTime = startTimeMillis,
    endTime = endTimeMillis,
    isOngoing = endTimeMillis == null,
)

fun defaultNewEnergyLevel(latestOverallEntry: EnergyEntry?): Int =
    latestOverallEntry?.energyLevel ?: 5

fun defaultManualActivityEditorState(
    selectedDate: LocalDate,
    today: LocalDate,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): ActivityEditorState {
    val startTime = if (selectedDate == today) {
        Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalTime().withSecond(0).withNano(0)
    } else {
        LocalTime.of(9, 0)
    }
    val endTime = startTime.plusHours(1)
    val endsNextDay = selectedDate == today && endTime <= startTime
    return ActivityEditorState(
        isVisible = true,
        mode = ActivityEditorMode.AddManual,
        startTime = startTime,
        endTime = endTime,
        endsNextDay = endsNextDay,
    )
}

fun formatTime(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).format(timeFormatter)

fun formatDateTime(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).format(dateTimeFormatter)

fun formatActivityDate(date: LocalDate): String = date.format(activityDateFormatter)

fun formatLocalTime(time: LocalTime): String = time.format(timeFormatter)

fun formatTimelineTimeRange(
    startTimeMillis: Long,
    endTimeMillis: Long?,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val start = formatTime(startTimeMillis, zoneId)
    val end = endTimeMillis?.let { formatTime(it, zoneId) } ?: "Now"
    return "$start - $end"
}

fun localDateAt(timestamp: Long, zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()

fun timeOnDateMillis(
    date: LocalDate,
    time: LocalTime,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Long = date.atTime(time).atZone(zoneId).toInstant().toEpochMilli()

fun activityEndMillis(entry: ActivityEntry, nowMillis: Long): Long =
    entry.endTime ?: max(nowMillis, entry.startTime + 60_000)

fun validateActivityInput(
    title: String,
    startTimeMillis: Long,
    endTimeMillis: Long?,
): String? {
    if (title.isBlank()) return "Title is required."
    if (endTimeMillis != null && endTimeMillis <= startTimeMillis) {
        return "End time must be after start time."
    }
    return null
}

fun activityIntervalsOverlap(
    existingStart: Long,
    existingEnd: Long?,
    candidateStart: Long,
    candidateEnd: Long?,
): Boolean {
    val existingEffectiveEnd = existingEnd ?: Long.MAX_VALUE
    val candidateEffectiveEnd = candidateEnd ?: Long.MAX_VALUE
    return existingStart < candidateEffectiveEnd && existingEffectiveEnd > candidateStart
}

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

private fun buildActivityBlock(
    entry: ActivityEntry,
    selectedDate: LocalDate,
    nowMillis: Long,
    zoneId: ZoneId,
): DailyTimelineItem.ActivityBlock? {
    val dayStart = selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val dayEnd = selectedDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val entryEnd = activityEndMillis(entry, nowMillis)
    if (entry.startTime >= dayEnd || entryEnd <= dayStart) return null

    val clippedStart = max(entry.startTime, dayStart)
    val clippedEnd = minOf(entryEnd, dayEnd)
    val totalDayMillis = (dayEnd - dayStart).toFloat()
    val topFraction = ((clippedStart - dayStart) / totalDayMillis).coerceIn(0f, 1f)
    val heightFraction = ((clippedEnd - clippedStart) / totalDayMillis).coerceAtLeast(MIN_BLOCK_FRACTION)
    val finalHeightFraction = heightFraction.coerceAtMost(1f - topFraction)

    return DailyTimelineItem.ActivityBlock(
        entry = entry,
        topFraction = topFraction,
        heightFraction = finalHeightFraction,
        title = entry.title,
        notePreview = entry.note.trim().takeIf(String::isNotEmpty),
        timeRangeText = formatTimelineTimeRange(entry.startTime, entry.endTime, zoneId),
        isOngoing = entry.isOngoing,
        contentMode = activityBlockContentMode(finalHeightFraction),
    )
}

private fun activityBlockContentMode(heightFraction: Float): ActivityBlockContentMode {
    val heightDp = heightFraction * TIMELINE_HEIGHT_DP
    return when {
        heightDp >= FULL_BLOCK_HEIGHT_DP -> ActivityBlockContentMode.Full
        heightDp >= COMPACT_BLOCK_HEIGHT_DP -> ActivityBlockContentMode.Compact
        heightDp >= TIME_ONLY_BLOCK_HEIGHT_DP -> ActivityBlockContentMode.TimeOnly
        else -> ActivityBlockContentMode.None
    }
}

private fun buildEnergyMarker(
    entry: EnergyEntry,
    selectedDate: LocalDate,
    zoneId: ZoneId,
): DailyTimelineItem.EnergyMarker? {
    if (localDateAt(entry.timestamp, zoneId) != selectedDate) return null
    val startOfDay = selectedDate.atStartOfDay(zoneId)
    val minutes = java.time.Duration.between(startOfDay, Instant.ofEpochMilli(entry.timestamp).atZone(zoneId)).toMinutes()
    val topFraction = (minutes / MINUTES_PER_DAY.toFloat()).coerceIn(0f, 1f)
    return DailyTimelineItem.EnergyMarker(
        entry = entry,
        topFraction = topFraction,
        scoreColor = energyScoreColor(entry.energyLevel),
    )
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color =
    Color(
        red = start.red + ((end.red - start.red) * fraction),
        green = start.green + ((end.green - start.green) * fraction),
        blue = start.blue + ((end.blue - start.blue) * fraction),
        alpha = start.alpha + ((end.alpha - start.alpha) * fraction),
    )

private const val NOTE_PREVIEW_CHARACTER_LIMIT = 15
private const val MINUTES_PER_DAY = 24 * 60
private const val TIMELINE_ROW_COUNT = 48
private const val GRID_ROW_HEIGHT_DP = 44f
private const val TIMELINE_HEIGHT_DP = GRID_ROW_HEIGHT_DP * TIMELINE_ROW_COUNT
private const val MIN_BLOCK_HEIGHT_DP = 10f
private const val FULL_BLOCK_HEIGHT_DP = 72f
private const val COMPACT_BLOCK_HEIGHT_DP = 44f
private const val TIME_ONLY_BLOCK_HEIGHT_DP = 22f
private const val MIN_BLOCK_FRACTION = MIN_BLOCK_HEIGHT_DP / TIMELINE_HEIGHT_DP
