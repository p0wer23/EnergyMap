package com.punith.energymap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.punith.energymap.R
import com.punith.energymap.data.EnergyEntry

@Composable
fun EnergyMapApp(
    viewModel: EnergyMapViewModel = viewModel(factory = EnergyMapViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        EnergyMapScreen(
            uiState = uiState,
            onAddEnergyClick = viewModel::onAddEnergyClick,
            onEditEnergyClick = viewModel::onEditEnergyClick,
            onEditorLevelChange = viewModel::onEditorLevelChange,
            onEditorNoteChange = viewModel::onEditorNoteChange,
            onSaveEnergy = viewModel::onSaveEnergy,
            onRequestDelete = viewModel::onRequestDelete,
            onConfirmDelete = viewModel::onConfirmDelete,
            onDismissDialogs = viewModel::onDismissDialogs,
        )
    }
}

@Composable
fun EnergyMapScreen(
    uiState: EnergyMapUiState,
    onAddEnergyClick: () -> Unit,
    onEditEnergyClick: (EnergyEntry) -> Unit,
    onEditorLevelChange: (Int) -> Unit,
    onEditorNoteChange: (String) -> Unit,
    onSaveEnergy: () -> Unit,
    onRequestDelete: (EnergyEntry) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDialogs: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            CurrentEnergyCard(
                currentEnergy = uiState.currentEnergy,
                latestOverallEntry = uiState.latestOverallEntry,
                onAddEnergyClick = onAddEnergyClick,
            )
        }

        item {
            SectionHeader(title = stringResource(id = R.string.today_check_ins_title))
        }

        if (uiState.todayEntries.isEmpty()) {
            item {
                EmptySectionCard(
                    message = stringResource(id = R.string.no_today_check_ins),
                )
            }
        } else {
            itemsIndexed(
                items = uiState.todayEntries,
                key = { _, entry -> entry.id },
            ) { index, entry ->
                EnergyEntryRow(
                    entry = entry,
                    isLatest = index == 0,
                    onClick = { onEditEnergyClick(entry) },
                )
            }
        }

        item {
            SectionHeader(title = stringResource(id = R.string.previous_days_title))
        }

        if (uiState.previousDaySections.isEmpty()) {
            item {
                EmptySectionCard(
                    message = stringResource(id = R.string.no_previous_check_ins),
                )
            }
        } else {
            itemsIndexed(
                items = uiState.previousDaySections,
                key = { _, section -> section.dateLabel },
            ) { _, section ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = section.dateLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    section.entries.forEach { entry ->
                        EnergyEntryRow(
                            entry = entry,
                            isLatest = false,
                            onClick = { onEditEnergyClick(entry) },
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (uiState.editorState.isVisible) {
        EnergyEditorDialog(
            editorState = uiState.editorState,
            onLevelChange = onEditorLevelChange,
            onNoteChange = onEditorNoteChange,
            onSave = onSaveEnergy,
            onDelete = {
                val editingEntry = (uiState.editorState.mode as? EnergyEditorMode.Edit)
                    ?.let { mode -> uiState.todayEntries.firstOrNull { it.id == mode.entryId } ?: uiState.previousDaySections.flatMap(EnergyDaySection::entries).firstOrNull { it.id == mode.entryId } }
                if (editingEntry != null) {
                    onRequestDelete(editingEntry)
                }
            },
            onDismiss = onDismissDialogs,
        )
    }

    uiState.pendingDeleteEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = onDismissDialogs,
            title = { Text(text = stringResource(id = R.string.delete_dialog_title)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.delete_dialog_message,
                        formatTime(entry.timestamp),
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    modifier = Modifier.testTag(EnergyMapTestTags.DELETE_CONFIRM_BUTTON),
                ) {
                    Text(text = stringResource(id = R.string.delete_action))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialogs) {
                    Text(text = stringResource(id = R.string.cancel_action))
                }
            },
        )
    }
}

@Composable
private fun CurrentEnergyCard(
    currentEnergy: EnergyEntry?,
    latestOverallEntry: EnergyEntry?,
    onAddEnergyClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.current_energy_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (currentEnergy == null) {
                Text(
                    text = stringResource(id = R.string.no_current_energy),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (latestOverallEntry != null) {
                    Text(
                        text = formatLastRecordedText(
                            timestamp = latestOverallEntry.timestamp,
                            nowMillis = System.currentTimeMillis(),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = stringResource(
                        id = R.string.energy_level_value,
                        currentEnergy.energyLevel,
                    ),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = energyBucketLabel(currentEnergy.energyLevel),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = formatDateTime(currentEnergy.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (currentEnergy.note.isNotBlank()) {
                    Text(
                        text = currentEnergy.note,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Button(
                onClick = onAddEnergyClick,
                modifier = Modifier.testTag(EnergyMapTestTags.ADD_CHECK_IN_BUTTON),
            ) {
                Text(text = stringResource(id = R.string.add_check_in_action))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun EmptySectionCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun EnergyEntryRow(
    entry: EnergyEntry,
    isLatest: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("${EnergyMapTestTags.ENERGY_ENTRY_PREFIX}${entry.id}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = formatTime(entry.timestamp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(
                            id = R.string.energy_entry_summary,
                            entry.energyLevel,
                            energyBucketLabel(entry.energyLevel),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (isLatest) {
                    AssistChip(
                        onClick = {},
                        label = { Text(text = stringResource(id = R.string.latest_chip)) },
                    )
                }
            }
            if (entry.note.isNotBlank()) {
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EnergyEditorDialog(
    editorState: EnergyEditorState,
    onLevelChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EnergyMapTestTags.ENERGY_EDITOR_DIALOG),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = when (editorState.mode) {
                        EnergyEditorMode.Add -> stringResource(id = R.string.new_check_in_title)
                        is EnergyEditorMode.Edit -> stringResource(id = R.string.edit_check_in_title)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(
                            id = R.string.energy_level_value,
                            editorState.level,
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Slider(
                        value = editorState.level.toFloat(),
                        onValueChange = { onLevelChange(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.testTag(EnergyMapTestTags.ENERGY_SLIDER),
                    )
                    Text(
                        text = energyBucketLabel(editorState.level),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                OutlinedTextField(
                    value = editorState.note,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EnergyMapTestTags.ENERGY_NOTE_FIELD),
                    minLines = 3,
                    label = { Text(text = stringResource(id = R.string.note_label)) },
                    placeholder = { Text(text = stringResource(id = R.string.note_placeholder)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )

                editorState.timestampText?.let { timestampText ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        HorizontalDivider()
                        Text(
                            text = stringResource(id = R.string.recorded_at_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = timestampText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (editorState.mode is EnergyEditorMode.Edit) {
                        TextButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag(EnergyMapTestTags.DELETE_CHECK_IN_BUTTON),
                        ) {
                            Text(text = stringResource(id = R.string.delete_action))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.cancel_action))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        modifier = Modifier.testTag(EnergyMapTestTags.SAVE_CHECK_IN_BUTTON),
                    ) {
                        Text(text = stringResource(id = R.string.save_action))
                    }
                }
            }
        }
    }
}
