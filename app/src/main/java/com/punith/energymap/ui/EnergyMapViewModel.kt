package com.punith.energymap.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.punith.energymap.data.ActivityEntry
import com.punith.energymap.data.ActivitySaveResult
import com.punith.energymap.data.AppDatabase
import com.punith.energymap.data.EnergyEntry
import com.punith.energymap.data.EnergyRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EnergyMapViewModel(
    private val repository: EnergyRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private var latestEnergyEntries: List<EnergyEntry> = emptyList()
    private var latestActivityEntries: List<ActivityEntry> = emptyList()

    private val currentView = MutableStateFlow(EnergyMapView.CheckIns)
    private val energyEditorState = MutableStateFlow(EnergyEditorState())
    private val pendingDeleteEnergyEntry = MutableStateFlow<EnergyEntry?>(null)
    private val selectedEntryFilter = MutableStateFlow(EnergyEntryFilter.TODAY)
    private val expandedEntryId = MutableStateFlow<Long?>(null)
    private val selectedActivityDate = MutableStateFlow(today())
    private val activityEditorState = MutableStateFlow(ActivityEditorState())
    private val quickStartActivityState = MutableStateFlow(QuickStartActivityState())
    private val pendingDeleteActivityEntry = MutableStateFlow<ActivityEntry?>(null)
    private val activityValidationMessage = MutableStateFlow<String?>(null)

    private val energyEntriesFlow = repository.observeEnergyEntries().onEach { latestEnergyEntries = it }
    private val activityEntriesFlow = repository.observeActivityEntries().onEach { latestActivityEntries = it }

    val uiState: StateFlow<EnergyMapUiState> =
        combine(
            energyEntriesFlow,
            activityEntriesFlow,
            currentView,
            energyEditorState,
            pendingDeleteEnergyEntry,
            selectedEntryFilter,
            expandedEntryId,
            selectedActivityDate,
            activityEditorState,
            quickStartActivityState,
            pendingDeleteActivityEntry,
            activityValidationMessage,
        ) { values ->
            val energyEntries = values[0] as List<EnergyEntry>
            val activityEntries = values[1] as List<ActivityEntry>
            val currentView = values[2] as EnergyMapView
            val energyEditor = values[3] as EnergyEditorState
            val pendingEnergyDelete = values[4] as EnergyEntry?
            val filter = values[5] as EnergyEntryFilter
            val expandedId = values[6] as Long?
            val selectedDate = values[7] as LocalDate
            val activityEditor = values[8] as ActivityEditorState
            val quickStart = values[9] as QuickStartActivityState
            val pendingActivityDelete = values[10] as ActivityEntry?
            val validationMessage = values[11] as String?

            val now = currentTimeMillis()
            val energyState = deriveEnergyState(energyEntries, now, zoneId)
            val activityState = deriveActivityTimelineState(
                activityEntries = activityEntries,
                energyEntries = energyEntries,
                selectedDate = selectedDate,
                nowMillis = now,
                zoneId = zoneId,
            )

            EnergyMapUiState(
                currentView = currentView,
                currentEnergy = energyState.currentEnergy,
                latestOverallEntry = energyState.latestOverallEntry,
                hasCheckInToday = energyState.todayEntries.isNotEmpty(),
                todayEntries = energyState.todayEntries,
                previousEntries = energyState.previousEntries,
                selectedEntryFilter = filter,
                expandedEntryId = expandedId,
                editorState = energyEditor,
                pendingDeleteEntry = pendingEnergyDelete,
                selectedActivityDate = selectedDate,
                activityEntries = activityEntries,
                currentActivity = activityState.currentActivity,
                dailyTimelineItems = activityState.timelineItems,
                activityEditorState = activityEditor,
                quickStartActivityState = quickStart,
                pendingDeleteActivityEntry = pendingActivityDelete,
                activityValidationMessage = validationMessage,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EnergyMapUiState(selectedActivityDate = today()),
        )

    fun onAddEnergyClick() {
        val latestEntry = deriveEnergyState(latestEnergyEntries, currentTimeMillis(), zoneId).latestOverallEntry
        energyEditorState.value = EnergyEditorState(
            isVisible = true,
            mode = EnergyEditorMode.Add,
            level = defaultNewEnergyLevel(latestEntry),
        )
    }

    fun onShowActivityView() {
        currentView.value = EnergyMapView.ActivityTimeline
        selectedActivityDate.value = today()
        activityValidationMessage.value = null
    }

    fun onShowCheckInsView() {
        currentView.value = EnergyMapView.CheckIns
        activityValidationMessage.value = null
    }

    fun onEditEnergyClick(entry: EnergyEntry) {
        energyEditorState.value = EnergyEditorState(
            isVisible = true,
            mode = EnergyEditorMode.Edit(entry.id),
            level = entry.energyLevel,
            note = entry.note,
            timestampText = formatDateTime(entry.timestamp, zoneId),
        )
    }

    fun onEditorLevelChange(level: Int) {
        energyEditorState.value = energyEditorState.value.copy(level = level.coerceIn(1, 10))
    }

    fun onEditorNoteChange(note: String) {
        energyEditorState.value = energyEditorState.value.copy(note = note)
    }

    fun onSaveEnergy() {
        val currentEditor = energyEditorState.value
        viewModelScope.launch {
            when (val mode = currentEditor.mode) {
                EnergyEditorMode.Add -> {
                    repository.saveEnergyEntry(
                        EnergyEntry(
                            timestamp = currentTimeMillis(),
                            energyLevel = currentEditor.level,
                            note = currentEditor.note.trim(),
                        ),
                    )
                }

                is EnergyEditorMode.Edit -> {
                    val existingEntry = latestEnergyEntries.firstOrNull { it.id == mode.entryId } ?: return@launch
                    repository.saveEnergyEntry(
                        buildUpdatedEnergyEntry(
                            existing = existingEntry,
                            newLevel = currentEditor.level,
                            newNote = currentEditor.note.trim(),
                        ),
                    )
                }
            }
            energyEditorState.value = EnergyEditorState()
            pendingDeleteEnergyEntry.value = null
            selectedEntryFilter.value = EnergyEntryFilter.TODAY
            expandedEntryId.value = null
        }
    }

    fun onEntryFilterChange(filter: EnergyEntryFilter) {
        selectedEntryFilter.value = filter
    }

    fun onToggleExpandedEntry(entryId: Long) {
        expandedEntryId.value = if (expandedEntryId.value == entryId) null else entryId
    }

    fun onRequestDelete(entry: EnergyEntry) {
        pendingDeleteEnergyEntry.value = entry
        energyEditorState.value = energyEditorState.value.copy(isVisible = false)
    }

    fun onConfirmDelete() {
        val entryToDelete = pendingDeleteEnergyEntry.value ?: return
        viewModelScope.launch {
            repository.deleteEnergyEntry(entryToDelete)
            dismissEnergyUi()
        }
    }

    fun onSelectPreviousActivityDate() {
        selectedActivityDate.value = selectedActivityDate.value.minusDays(1)
        activityValidationMessage.value = null
    }

    fun onSelectNextActivityDate() {
        val next = selectedActivityDate.value.plusDays(1)
        if (next <= today()) {
            selectedActivityDate.value = next
            activityValidationMessage.value = null
        }
    }

    fun onSelectTodayActivityDate() {
        selectedActivityDate.value = today()
        activityValidationMessage.value = null
    }

    fun onOpenStartActivityDialog() {
        if (selectedActivityDate.value != today()) return
        quickStartActivityState.value = QuickStartActivityState(isVisible = true)
        activityValidationMessage.value = null
    }

    fun onQuickStartTitleChange(title: String) {
        quickStartActivityState.value = quickStartActivityState.value.copy(title = title, validationMessage = null)
    }

    fun onQuickStartNoteChange(note: String) {
        quickStartActivityState.value = quickStartActivityState.value.copy(note = note, validationMessage = null)
    }

    fun onConfirmStartActivity() {
        val state = quickStartActivityState.value
        val validationMessage = validateActivityInput(
            title = state.title.trim(),
            startTimeMillis = currentTimeMillis(),
            endTimeMillis = null,
        )
        if (validationMessage != null) {
            quickStartActivityState.value = state.copy(validationMessage = validationMessage)
            return
        }

        viewModelScope.launch {
            when (
                repository.startActivity(
                    title = state.title.trim(),
                    note = state.note.trim(),
                    now = currentTimeMillis(),
                )
            ) {
                ActivitySaveResult.Success -> {
                    quickStartActivityState.value = QuickStartActivityState()
                    activityValidationMessage.value = null
                }

                ActivitySaveResult.Overlap -> {
                    activityValidationMessage.value = OVERLAP_MESSAGE
                }

                ActivitySaveResult.NoActiveEntry -> Unit
            }
        }
    }

    fun onEndCurrentActivity() {
        viewModelScope.launch {
            when (repository.endCurrentActivity(currentTimeMillis())) {
                ActivitySaveResult.Success -> activityValidationMessage.value = null
                ActivitySaveResult.NoActiveEntry -> activityValidationMessage.value = null
                ActivitySaveResult.Overlap -> activityValidationMessage.value = OVERLAP_MESSAGE
            }
        }
    }

    fun onOpenManualActivityDialog() {
        activityEditorState.value = defaultManualActivityEditorState(selectedActivityDate.value, today())
        activityValidationMessage.value = null
    }

    fun onEditActivityClick(entry: ActivityEntry) {
        val start = Instant.ofEpochMilli(entry.startTime).atZone(zoneId).toLocalTime()
        val end = entry.endTime?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalTime() } ?: start.plusHours(1)
        val entryDate = localDateAt(entry.startTime, zoneId)
        val endsNextDay = entry.endTime?.let { localDateAt(it, zoneId) > entryDate } ?: false
        selectedActivityDate.value = entryDate
        activityEditorState.value = ActivityEditorState(
            isVisible = true,
            mode = ActivityEditorMode.Edit(entry.id),
            title = entry.title,
            note = entry.note,
            startTime = start,
            endTime = end,
            endsNextDay = endsNextDay,
        )
        activityValidationMessage.value = null
    }

    fun onActivityTitleChange(title: String) {
        activityEditorState.value = activityEditorState.value.copy(title = title, validationMessage = null)
    }

    fun onActivityNoteChange(note: String) {
        activityEditorState.value = activityEditorState.value.copy(note = note, validationMessage = null)
    }

    fun onActivityTimeFieldClick(field: ActivityTimeField) {
        activityEditorState.value = activityEditorState.value.copy(timePickerField = field)
    }

    fun onActivityTimeSelected(time: LocalTime) {
        val editor = activityEditorState.value
        activityEditorState.value = when (editor.timePickerField) {
            ActivityTimeField.Start -> editor.copy(startTime = time, timePickerField = null, validationMessage = null)
            ActivityTimeField.End -> editor.copy(endTime = time, timePickerField = null, validationMessage = null)
            null -> editor
        }
    }

    fun onDismissActivityTimePicker() {
        activityEditorState.value = activityEditorState.value.copy(timePickerField = null)
    }

    fun onEndsNextDayChange(endsNextDay: Boolean) {
        activityEditorState.value = activityEditorState.value.copy(endsNextDay = endsNextDay, validationMessage = null)
    }

    fun onSaveActivity() {
        val editor = activityEditorState.value
        val startTimeMillis = timeOnDateMillis(selectedActivityDate.value, editor.startTime, zoneId)
        val endDate = if (editor.endsNextDay) selectedActivityDate.value.plusDays(1) else selectedActivityDate.value
        val endTimeMillis = timeOnDateMillis(endDate, editor.endTime, zoneId)
        val validationMessage = validateActivityInput(editor.title.trim(), startTimeMillis, endTimeMillis)
        if (validationMessage != null) {
            activityEditorState.value = editor.copy(validationMessage = validationMessage)
            return
        }

        viewModelScope.launch {
            val result = when (val mode = editor.mode) {
                ActivityEditorMode.AddManual -> {
                    repository.saveActivityEntry(
                        ActivityEntry(
                            title = editor.title.trim(),
                            startTime = startTimeMillis,
                            endTime = endTimeMillis,
                            note = editor.note.trim(),
                            isOngoing = false,
                        ),
                        excludeId = null,
                    )
                }

                is ActivityEditorMode.Edit -> {
                    val existing = latestActivityEntries.firstOrNull { it.id == mode.entryId } ?: return@launch
                    repository.saveActivityEntry(
                        buildUpdatedActivityEntry(
                            existing = existing,
                            title = editor.title.trim(),
                            note = editor.note.trim(),
                            startTimeMillis = startTimeMillis,
                            endTimeMillis = endTimeMillis,
                        ),
                        excludeId = existing.id,
                    )
                }
            }

            when (result) {
                ActivitySaveResult.Success -> {
                    activityEditorState.value = ActivityEditorState()
                    pendingDeleteActivityEntry.value = null
                    activityValidationMessage.value = null
                }

                ActivitySaveResult.Overlap -> {
                    activityEditorState.value = editor.copy(validationMessage = OVERLAP_MESSAGE)
                    activityValidationMessage.value = OVERLAP_MESSAGE
                }

                ActivitySaveResult.NoActiveEntry -> Unit
            }
        }
    }

    fun onRequestDeleteActivity(entry: ActivityEntry) {
        pendingDeleteActivityEntry.value = entry
        activityEditorState.value = activityEditorState.value.copy(isVisible = false)
    }

    fun onConfirmDeleteActivity() {
        val entry = pendingDeleteActivityEntry.value ?: return
        viewModelScope.launch {
            repository.deleteActivityEntry(entry)
            pendingDeleteActivityEntry.value = null
            activityEditorState.value = ActivityEditorState()
            activityValidationMessage.value = null
        }
    }

    fun onDismissDialogs() {
        dismissEnergyUi()
        quickStartActivityState.value = QuickStartActivityState()
        activityEditorState.value = ActivityEditorState()
        pendingDeleteActivityEntry.value = null
    }

    private fun dismissEnergyUi() {
        energyEditorState.value = EnergyEditorState()
        pendingDeleteEnergyEntry.value = null
    }

    private fun today(): LocalDate = localDateAt(currentTimeMillis(), zoneId)

    companion object {
        private const val OVERLAP_MESSAGE = "Activity overlaps an existing entry."

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as Application
                val database = AppDatabase.getInstance(application)
                val repository = EnergyRepository(
                    energyDao = database.energyDao(),
                    activityDao = database.activityDao(),
                )
                return EnergyMapViewModel(repository) as T
            }
        }
    }
}
