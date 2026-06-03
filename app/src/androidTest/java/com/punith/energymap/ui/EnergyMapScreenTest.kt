package com.punith.energymap.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.punith.energymap.data.ActivityEntry
import com.punith.energymap.data.EnergyEntry
import com.punith.energymap.ui.theme.EnergyMapTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnergyMapScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val zoneId: ZoneId = ZoneId.of("UTC")
    private val todayDate: LocalDate = LocalDate.now(zoneId)
    private val previousDate: LocalDate = todayDate.minusDays(1)

    @Test
    fun energyCheckInFlowWorks() {
        val nowMillis = todayDate.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()
        val longNote = "1234567890123456"
        val longNotePreview = "123456789012345..."

        composeRule.setContent {
            EnergyMapTheme {
                var currentView by remember { mutableStateOf(EnergyMapView.CheckIns) }
                var energyEntries by remember {
                    mutableStateOf(
                        listOf(
                            EnergyEntry(
                                id = 99,
                                timestamp = previousDate.atTime(8, 0).atZone(zoneId).toInstant().toEpochMilli(),
                                energyLevel = 4,
                                note = "Previous day note",
                            ),
                        ),
                    )
                }
                var activityEntries by remember { mutableStateOf(emptyList<ActivityEntry>()) }
                var energyEditorState by remember { mutableStateOf(EnergyEditorState()) }
                var pendingDeleteEnergyEntry by remember { mutableStateOf<EnergyEntry?>(null) }
                var selectedFilter by remember { mutableStateOf(EnergyEntryFilter.TODAY) }
                var expandedEntryId by remember { mutableStateOf<Long?>(null) }
                var selectedActivityDate by remember { mutableStateOf(todayDate) }
                var activityEditorState by remember { mutableStateOf(ActivityEditorState()) }
                var quickStartState by remember { mutableStateOf(QuickStartActivityState()) }
                var pendingDeleteActivity by remember { mutableStateOf<ActivityEntry?>(null) }
                var activityValidationMessage by remember { mutableStateOf<String?>(null) }
                var nextEnergyId by remember { mutableLongStateOf(1L) }

                fun dismissDialogs() {
                    energyEditorState = EnergyEditorState()
                    pendingDeleteEnergyEntry = null
                    activityEditorState = ActivityEditorState()
                    quickStartState = QuickStartActivityState()
                    pendingDeleteActivity = null
                }

                val energyState = deriveEnergyState(
                    entries = energyEntries,
                    nowMillis = nowMillis,
                    zoneId = zoneId,
                )
                val activityState = deriveActivityTimelineState(
                    activityEntries = activityEntries,
                    energyEntries = energyEntries,
                    selectedDate = selectedActivityDate,
                    nowMillis = nowMillis,
                    zoneId = zoneId,
                )

                EnergyMapScreen(
                    uiState = EnergyMapUiState(
                        currentView = currentView,
                        currentEnergy = energyState.currentEnergy,
                        latestOverallEntry = energyState.latestOverallEntry,
                        hasCheckInToday = energyState.todayEntries.isNotEmpty(),
                        todayEntries = energyState.todayEntries,
                        previousEntries = energyState.previousEntries,
                        selectedEntryFilter = selectedFilter,
                        expandedEntryId = expandedEntryId,
                        editorState = energyEditorState,
                        pendingDeleteEntry = pendingDeleteEnergyEntry,
                        selectedActivityDate = selectedActivityDate,
                        activityEntries = activityEntries,
                        currentActivity = activityState.currentActivity,
                        dailyTimelineItems = activityState.timelineItems,
                        activityEditorState = activityEditorState,
                        quickStartActivityState = quickStartState,
                        pendingDeleteActivityEntry = pendingDeleteActivity,
                        activityValidationMessage = activityValidationMessage,
                    ),
                    onAddEnergyClick = {
                        energyEditorState = EnergyEditorState(
                            isVisible = true,
                            mode = EnergyEditorMode.Add,
                            level = energyState.latestOverallEntry?.energyLevel ?: 5,
                        )
                    },
                    onShowActivityView = { currentView = EnergyMapView.ActivityTimeline },
                    onShowCheckInsView = { currentView = EnergyMapView.CheckIns },
                    onEditEnergyClick = { entry ->
                        energyEditorState = EnergyEditorState(
                            isVisible = true,
                            mode = EnergyEditorMode.Edit(entry.id),
                            level = entry.energyLevel,
                            note = entry.note,
                            timestampText = formatDateTime(entry.timestamp, zoneId),
                        )
                    },
                    onEntryFilterChange = { selectedFilter = it },
                    onToggleExpandedEntry = { entryId ->
                        expandedEntryId = if (expandedEntryId == entryId) null else entryId
                    },
                    onEditorLevelChange = { level -> energyEditorState = energyEditorState.copy(level = level) },
                    onEditorNoteChange = { note -> energyEditorState = energyEditorState.copy(note = note) },
                    onSaveEnergy = {
                        when (val mode = energyEditorState.mode) {
                            EnergyEditorMode.Add -> {
                                energyEntries = (energyEntries + EnergyEntry(
                                    id = nextEnergyId,
                                    timestamp = nowMillis,
                                    energyLevel = energyEditorState.level,
                                    note = energyEditorState.note.trim(),
                                )).sortedByDescending(EnergyEntry::timestamp)
                                nextEnergyId += 1
                            }

                            is EnergyEditorMode.Edit -> {
                                energyEntries = energyEntries.map { entry ->
                                    if (entry.id == mode.entryId) {
                                        buildUpdatedEnergyEntry(entry, energyEditorState.level, energyEditorState.note.trim())
                                    } else {
                                        entry
                                    }
                                }
                            }
                        }
                        selectedFilter = EnergyEntryFilter.TODAY
                        expandedEntryId = null
                        dismissDialogs()
                    },
                    onRequestDelete = { entry ->
                        pendingDeleteEnergyEntry = entry
                        energyEditorState = energyEditorState.copy(isVisible = false)
                    },
                    onConfirmDelete = {
                        val deleteId = pendingDeleteEnergyEntry?.id
                        energyEntries = energyEntries.filterNot { it.id == deleteId }
                        dismissDialogs()
                    },
                    onSelectPreviousActivityDate = { selectedActivityDate = selectedActivityDate.minusDays(1) },
                    onSelectNextActivityDate = { selectedActivityDate = selectedActivityDate.plusDays(1) },
                    onSelectTodayActivityDate = { selectedActivityDate = todayDate },
                    onOpenStartActivityDialog = {},
                    onQuickStartTitleChange = {},
                    onQuickStartNoteChange = {},
                    onConfirmStartActivity = {},
                    onEndCurrentActivity = {},
                    onOpenManualActivityDialog = {},
                    onEditActivityClick = {},
                    onActivityTitleChange = {},
                    onActivityNoteChange = {},
                    onActivityTimeFieldClick = {},
                    onActivityTimeSelected = {},
                    onDismissActivityTimePicker = {},
                    onEndsNextDayChange = {},
                    onSaveActivity = {},
                    onRequestDeleteActivity = {},
                    onConfirmDeleteActivity = {},
                    onDismissDialogs = ::dismissDialogs,
                )
            }
        }

        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_CHECK_INS_HEADER).assertIsDisplayed()
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_VIEW_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithText("No check-ins recorded for today.").assertIsDisplayed()

        composeRule.onNodeWithTag(EnergyMapTestTags.ADD_CHECK_IN_BUTTON).performClick()
        composeRule.onNodeWithText("4").assertIsDisplayed()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_NOTE_FIELD).performTextInput(longNote)
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_SLIDER)
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress -> setProgress(8f) }
        composeRule.onNodeWithTag(EnergyMapTestTags.SAVE_CHECK_IN_BUTTON).performClick()

        composeRule.onNodeWithTag(EnergyMapTestTags.LATEST_TODAY_ENTRY).assertIsDisplayed()
        composeRule.onNodeWithText(longNotePreview).assertIsDisplayed()
        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_NOTE_PREFIX}1").performClick()
        composeRule.onNodeWithText(longNote).assertIsDisplayed()

        composeRule.onNodeWithTag(EnergyMapTestTags.PREVIOUS_FILTER_BUTTON).performClick()
        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_PREFIX}99").assertIsDisplayed()

        composeRule.onNodeWithTag(EnergyMapTestTags.TODAY_FILTER_BUTTON).performClick()
        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_EDIT_PREFIX}1").performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_NOTE_FIELD).performTextClearance()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_NOTE_FIELD).performTextInput("Updated check-in")
        composeRule.onNodeWithTag(EnergyMapTestTags.SAVE_CHECK_IN_BUTTON).performClick()
        composeRule.onNodeWithText("Updated check-in").assertIsDisplayed()

        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_EDIT_PREFIX}1").performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.DELETE_CHECK_IN_BUTTON).performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.DELETE_CONFIRM_BUTTON).performClick()
        composeRule.onNodeWithText("No check-ins recorded for today.").assertIsDisplayed()
    }

    @Test
    fun activityTimelineFlowWorks() {
        val nowMillis = todayDate.atTime(10, 30).atZone(zoneId).toInstant().toEpochMilli()
        val previousEnergy = EnergyEntry(
            id = 9,
            timestamp = previousDate.atTime(11, 0).atZone(zoneId).toInstant().toEpochMilli(),
            energyLevel = 6,
            note = "",
        )
        val previousActivity = ActivityEntry(
            id = 7,
            title = "Previous walk",
            startTime = previousDate.atTime(9, 0).atZone(zoneId).toInstant().toEpochMilli(),
            endTime = previousDate.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli(),
            note = "Outdoor",
            isOngoing = false,
        )

        composeRule.setContent {
            EnergyMapTheme {
                var currentView by remember { mutableStateOf(EnergyMapView.CheckIns) }
                var energyEntries by remember { mutableStateOf(listOf(previousEnergy)) }
                var activityEntries by remember { mutableStateOf(listOf(previousActivity)) }
                var energyEditorState by remember { mutableStateOf(EnergyEditorState()) }
                var pendingDeleteEnergyEntry by remember { mutableStateOf<EnergyEntry?>(null) }
                var selectedFilter by remember { mutableStateOf(EnergyEntryFilter.TODAY) }
                var expandedEntryId by remember { mutableStateOf<Long?>(null) }
                var selectedActivityDate by remember { mutableStateOf(todayDate) }
                var activityEditorState by remember { mutableStateOf(ActivityEditorState()) }
                var quickStartState by remember { mutableStateOf(QuickStartActivityState()) }
                var pendingDeleteActivity by remember { mutableStateOf<ActivityEntry?>(null) }
                var activityValidationMessage by remember { mutableStateOf<String?>(null) }
                var nextActivityId by remember { mutableLongStateOf(20L) }

                fun dismissDialogs() {
                    energyEditorState = EnergyEditorState()
                    pendingDeleteEnergyEntry = null
                    activityEditorState = ActivityEditorState()
                    quickStartState = QuickStartActivityState()
                    pendingDeleteActivity = null
                }

                fun saveActivity(entry: ActivityEntry, excludeId: Long? = entry.takeIf { it.id != 0L }?.id): Boolean {
                    val hasOverlap = activityEntries.any { existing ->
                        existing.id != excludeId &&
                            activityIntervalsOverlap(
                                existingStart = existing.startTime,
                                existingEnd = existing.endTime,
                                candidateStart = entry.startTime,
                                candidateEnd = entry.endTime,
                            )
                    }
                    if (hasOverlap) return false
                    activityEntries = (activityEntries.filterNot { it.id == entry.id } + entry).sortedByDescending(ActivityEntry::startTime)
                    return true
                }

                val energyState = deriveEnergyState(
                    entries = energyEntries,
                    nowMillis = nowMillis,
                    zoneId = zoneId,
                )
                val activityState = deriveActivityTimelineState(
                    activityEntries = activityEntries,
                    energyEntries = energyEntries,
                    selectedDate = selectedActivityDate,
                    nowMillis = nowMillis,
                    zoneId = zoneId,
                )

                EnergyMapScreen(
                    uiState = EnergyMapUiState(
                        currentView = currentView,
                        currentEnergy = energyState.currentEnergy,
                        latestOverallEntry = energyState.latestOverallEntry,
                        hasCheckInToday = energyState.todayEntries.isNotEmpty(),
                        todayEntries = energyState.todayEntries,
                        previousEntries = energyState.previousEntries,
                        selectedEntryFilter = selectedFilter,
                        expandedEntryId = expandedEntryId,
                        editorState = energyEditorState,
                        pendingDeleteEntry = pendingDeleteEnergyEntry,
                        selectedActivityDate = selectedActivityDate,
                        activityEntries = activityEntries,
                        currentActivity = activityState.currentActivity,
                        dailyTimelineItems = activityState.timelineItems,
                        activityEditorState = activityEditorState,
                        quickStartActivityState = quickStartState,
                        pendingDeleteActivityEntry = pendingDeleteActivity,
                        activityValidationMessage = activityValidationMessage,
                    ),
                    onAddEnergyClick = {},
                    onShowActivityView = { currentView = EnergyMapView.ActivityTimeline },
                    onShowCheckInsView = { currentView = EnergyMapView.CheckIns },
                    onEditEnergyClick = {},
                    onEntryFilterChange = { selectedFilter = it },
                    onToggleExpandedEntry = { expandedEntryId = it },
                    onEditorLevelChange = {},
                    onEditorNoteChange = {},
                    onSaveEnergy = {},
                    onRequestDelete = { pendingDeleteEnergyEntry = it },
                    onConfirmDelete = {},
                    onSelectPreviousActivityDate = { selectedActivityDate = selectedActivityDate.minusDays(1) },
                    onSelectNextActivityDate = {
                        if (selectedActivityDate < todayDate) selectedActivityDate = selectedActivityDate.plusDays(1)
                    },
                    onSelectTodayActivityDate = { selectedActivityDate = todayDate },
                    onOpenStartActivityDialog = {
                        if (selectedActivityDate == todayDate) {
                            quickStartState = QuickStartActivityState(isVisible = true)
                        }
                    },
                    onQuickStartTitleChange = { quickStartState = quickStartState.copy(title = it, validationMessage = null) },
                    onQuickStartNoteChange = { quickStartState = quickStartState.copy(note = it, validationMessage = null) },
                    onConfirmStartActivity = {
                        if (quickStartState.title.isBlank()) {
                            quickStartState = quickStartState.copy(validationMessage = "Title is required.")
                        } else {
                            val success = saveActivity(
                                ActivityEntry(
                                    id = nextActivityId,
                                    title = quickStartState.title.trim(),
                                    startTime = nowMillis,
                                    endTime = null,
                                    note = quickStartState.note.trim(),
                                    isOngoing = true,
                                ),
                                excludeId = null,
                            )
                            if (success) {
                                nextActivityId += 1
                                quickStartState = QuickStartActivityState()
                                activityValidationMessage = null
                            } else {
                                activityValidationMessage = "Activity overlaps an existing entry."
                            }
                        }
                    },
                    onEndCurrentActivity = {
                        val current = activityEntries.firstOrNull { it.isOngoing }
                        if (current != null) {
                            activityEntries = activityEntries.map {
                                if (it.id == current.id) {
                                    it.copy(endTime = nowMillis + 60_000, isOngoing = false)
                                } else {
                                    it
                                }
                            }
                        }
                    },
                    onOpenManualActivityDialog = {
                        activityEditorState = ActivityEditorState(
                            isVisible = true,
                            mode = ActivityEditorMode.AddManual,
                            startTime = LocalTime.of(9, 0),
                            endTime = LocalTime.of(10, 0),
                        )
                    },
                    onEditActivityClick = { entry ->
                        selectedActivityDate = localDateAt(entry.startTime, zoneId)
                        activityEditorState = ActivityEditorState(
                            isVisible = true,
                            mode = ActivityEditorMode.Edit(entry.id),
                            title = entry.title,
                            note = entry.note,
                            startTime = java.time.Instant.ofEpochMilli(entry.startTime).atZone(zoneId).toLocalTime(),
                            endTime = java.time.Instant.ofEpochMilli(entry.endTime ?: entry.startTime + 3_600_000).atZone(zoneId).toLocalTime(),
                        )
                    },
                    onActivityTitleChange = { activityEditorState = activityEditorState.copy(title = it, validationMessage = null) },
                    onActivityNoteChange = { activityEditorState = activityEditorState.copy(note = it, validationMessage = null) },
                    onActivityTimeFieldClick = {},
                    onActivityTimeSelected = {},
                    onDismissActivityTimePicker = {},
                    onEndsNextDayChange = { activityEditorState = activityEditorState.copy(endsNextDay = it) },
                    onSaveActivity = {
                        val startMillis = timeOnDateMillis(selectedActivityDate, activityEditorState.startTime, zoneId)
                        val endDate = if (activityEditorState.endsNextDay) selectedActivityDate.plusDays(1) else selectedActivityDate
                        val endMillis = timeOnDateMillis(endDate, activityEditorState.endTime, zoneId)
                        val validation = validateActivityInput(activityEditorState.title.trim(), startMillis, endMillis)
                        if (validation != null) {
                            activityEditorState = activityEditorState.copy(validationMessage = validation)
                        } else {
                            val mode = activityEditorState.mode
                            val success = when (mode) {
                                ActivityEditorMode.AddManual -> saveActivity(
                                    ActivityEntry(
                                        id = nextActivityId,
                                        title = activityEditorState.title.trim(),
                                        startTime = startMillis,
                                        endTime = endMillis,
                                        note = activityEditorState.note.trim(),
                                        isOngoing = false,
                                    ),
                                    excludeId = null,
                                )

                                is ActivityEditorMode.Edit -> saveActivity(
                                    ActivityEntry(
                                        id = mode.entryId,
                                        title = activityEditorState.title.trim(),
                                        startTime = startMillis,
                                        endTime = endMillis,
                                        note = activityEditorState.note.trim(),
                                        isOngoing = false,
                                    ),
                                    excludeId = mode.entryId,
                                )
                            }
                            if (success) {
                                if (mode is ActivityEditorMode.AddManual) nextActivityId += 1
                                activityEditorState = ActivityEditorState()
                                activityValidationMessage = null
                            } else {
                                val message = "Activity overlaps an existing entry."
                                activityEditorState = activityEditorState.copy(validationMessage = message)
                                activityValidationMessage = message
                            }
                        }
                    },
                    onRequestDeleteActivity = { entry ->
                        pendingDeleteActivity = entry
                        activityEditorState = activityEditorState.copy(isVisible = false)
                    },
                    onConfirmDeleteActivity = {
                        val deleteId = pendingDeleteActivity?.id
                        activityEntries = activityEntries.filterNot { it.id == deleteId }
                        dismissDialogs()
                    },
                    onDismissDialogs = ::dismissDialogs,
                )
            }
        }

        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_VIEW_BUTTON).performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_TIMELINE_SCREEN).assertIsDisplayed()

        composeRule.onNodeWithTag(EnergyMapTestTags.START_ACTIVITY_BUTTON).performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_TITLE_FIELD).performTextInput("Focus")
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_SAVE_BUTTON).performClick()
        composeRule.onNodeWithTag("${EnergyMapTestTags.ACTIVITY_BLOCK_PREFIX}20").assertIsDisplayed()

        composeRule.onNodeWithTag(EnergyMapTestTags.END_CURRENT_ACTIVITY_BUTTON).performClick()
        composeRule.onNodeWithText("Focus").assertIsDisplayed()

        composeRule.onNodeWithTag(EnergyMapTestTags.MANUAL_ACTIVITY_BUTTON).performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_TITLE_FIELD).performTextInput("Overlap")
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_SAVE_BUTTON).performClick()
        composeRule.onNodeWithText("Activity overlaps an existing entry.").assertIsDisplayed()
        composeRule.onAllNodesWithTag("${EnergyMapTestTags.ACTIVITY_BLOCK_PREFIX}21").assertCountEquals(0)

        composeRule.onNodeWithTag("${EnergyMapTestTags.ACTIVITY_BLOCK_PREFIX}20").performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_NOTE_FIELD).performTextInput(" Updated")
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_SAVE_BUTTON).performClick()
        composeRule.onNodeWithText("Updated").assertIsDisplayed()

        composeRule.onNodeWithTag("${EnergyMapTestTags.ACTIVITY_BLOCK_PREFIX}20").performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_DELETE_BUTTON).performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ACTIVITY_DELETE_CONFIRM_BUTTON).performClick()
        composeRule.onAllNodesWithTag("${EnergyMapTestTags.ACTIVITY_BLOCK_PREFIX}20").assertCountEquals(0)

        composeRule.onNodeWithTag(EnergyMapTestTags.PREVIOUS_DAY_BUTTON).performClick()
        composeRule.onNodeWithTag("${EnergyMapTestTags.ACTIVITY_BLOCK_PREFIX}7").assertIsDisplayed()
        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_TIMELINE_MARKER_PREFIX}9").assertIsDisplayed()

        composeRule.onNodeWithTag(EnergyMapTestTags.BACK_TO_CHECK_INS_BUTTON).performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_CHECK_INS_HEADER).assertIsDisplayed()
    }
}
