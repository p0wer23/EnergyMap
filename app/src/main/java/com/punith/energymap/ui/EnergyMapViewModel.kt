package com.punith.energymap.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.punith.energymap.data.ActivityEntry
import com.punith.energymap.data.AppDatabase
import com.punith.energymap.data.EnergyEntry
import com.punith.energymap.data.EnergyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TimelineItem(
    val title: String,
    val subtitle: String,
)

data class EnergyMapUiState(
    val energyEntries: List<EnergyEntry> = emptyList(),
    val activityEntries: List<ActivityEntry> = emptyList(),
    val timelineItems: List<TimelineItem> = emptyList(),
)

class EnergyMapViewModel(
    private val repository: EnergyRepository,
) : ViewModel() {
    val uiState: StateFlow<EnergyMapUiState> =
        combine(
            repository.observeEnergyEntries(),
            repository.observeActivityEntries(),
        ) { energyEntries, activityEntries ->
            val timeline = buildList {
                energyEntries.forEach { add(TimelineItem("Energy ${it.energyLevel}/10", it.note.ifBlank { "No note" })) }
                activityEntries.forEach {
                    add(
                        TimelineItem(
                            title = it.title,
                            subtitle = if (it.isOngoing) "Ongoing activity" else it.note.ifBlank { "Completed activity" },
                        ),
                    )
                }
            }.sortedByDescending { it.title }

            EnergyMapUiState(
                energyEntries = energyEntries,
                activityEntries = activityEntries,
                timelineItems = timeline,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EnergyMapUiState(),
        )

    fun seedSampleData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.addEnergyEntry(
                EnergyEntry(
                    timestamp = now,
                    energyLevel = 7,
                    note = "Setup verification entry",
                ),
            )
            repository.addActivityEntry(
                ActivityEntry(
                    title = "Initial walkthrough",
                    startTime = now - 45 * 60 * 1000,
                    endTime = now,
                    note = "Created during project setup",
                    isOngoing = false,
                ),
            )
        }
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
