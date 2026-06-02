package com.punith.energymap.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.punith.energymap.data.AppDatabase
import com.punith.energymap.data.EnergyEntry
import com.punith.energymap.data.EnergyRepository
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

    private val editorState = MutableStateFlow(EnergyEditorState())
    private val pendingDeleteEntry = MutableStateFlow<EnergyEntry?>(null)

    private val energyEntriesFlow = repository.observeEnergyEntries().onEach { latestEnergyEntries = it }

    val uiState: StateFlow<EnergyMapUiState> =
        combine(
            energyEntriesFlow,
            editorState,
            pendingDeleteEntry,
        ) { energyEntries, editor, pendingDelete ->
            val derivedState = deriveEnergyState(
                entries = energyEntries,
                nowMillis = currentTimeMillis(),
                zoneId = zoneId,
            )
            EnergyMapUiState(
                currentEnergy = derivedState.currentEnergy,
                latestOverallEntry = derivedState.latestOverallEntry,
                hasCheckInToday = derivedState.todayEntries.isNotEmpty(),
                todayEntries = derivedState.todayEntries,
                previousDaySections = derivedState.previousDaySections,
                editorState = editor,
                pendingDeleteEntry = pendingDelete,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EnergyMapUiState(),
        )

    fun onAddEnergyClick() {
        val latestTodayEntry = deriveEnergyState(
            entries = latestEnergyEntries,
            nowMillis = currentTimeMillis(),
            zoneId = zoneId,
        ).todayEntries.firstOrNull()

        editorState.value = EnergyEditorState(
            isVisible = true,
            mode = EnergyEditorMode.Add,
            level = latestTodayEntry?.energyLevel ?: 5,
            note = "",
            timestampText = null,
        )
    }

    fun onEditEnergyClick(entry: EnergyEntry) {
        editorState.value = EnergyEditorState(
            isVisible = true,
            mode = EnergyEditorMode.Edit(entry.id),
            level = entry.energyLevel,
            note = entry.note,
            timestampText = formatDateTime(entry.timestamp, zoneId),
        )
    }

    fun onEditorLevelChange(level: Int) {
        editorState.value = editorState.value.copy(level = level.coerceIn(1, 10))
    }

    fun onEditorNoteChange(note: String) {
        editorState.value = editorState.value.copy(note = note)
    }

    fun onSaveEnergy() {
        val currentEditor = editorState.value
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
            onDismissDialogs()
        }
    }

    fun onRequestDelete(entry: EnergyEntry) {
        pendingDeleteEntry.value = entry
        editorState.value = editorState.value.copy(isVisible = false)
    }

    fun onConfirmDelete() {
        val entryToDelete = pendingDeleteEntry.value ?: return
        viewModelScope.launch {
            repository.deleteEnergyEntry(entryToDelete)
            onDismissDialogs()
        }
    }

    fun onDismissDialogs() {
        editorState.value = EnergyEditorState()
        pendingDeleteEntry.value = null
    }

    companion object {
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
