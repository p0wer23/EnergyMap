package com.punith.energymap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
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
            onEntryFilterChange = viewModel::onEntryFilterChange,
            onToggleExpandedEntry = viewModel::onToggleExpandedEntry,
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
    onEntryFilterChange: (EnergyEntryFilter) -> Unit,
    onToggleExpandedEntry: (Long) -> Unit,
    onEditorLevelChange: (Int) -> Unit,
    onEditorNoteChange: (String) -> Unit,
    onSaveEnergy: () -> Unit,
    onRequestDelete: (EnergyEntry) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDialogs: () -> Unit,
) {
    val visibleEntries = when (uiState.selectedEntryFilter) {
        EnergyEntryFilter.TODAY -> uiState.todayEntries
        EnergyEntryFilter.PREVIOUS -> uiState.previousEntries
    }
    val addEnergyContentDescription = stringResource(id = R.string.add_energy_content_description)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EnergyMapTestTags.ENERGY_CHECK_INS_HEADER),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.energy_history_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Box(modifier = Modifier.testTag(EnergyMapTestTags.ENERGY_CHECK_INS_HEADER_ACTION)) {
                        FilledTonalIconButton(
                            onClick = onAddEnergyClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag(EnergyMapTestTags.ADD_CHECK_IN_BUTTON)
                                .semantics {
                                    contentDescription = addEnergyContentDescription
                                },
                            colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Text(
                                text = stringResource(id = R.string.add_check_in_symbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                EntryFilterSelector(
                    selectedFilter = uiState.selectedEntryFilter,
                    onEntryFilterChange = onEntryFilterChange,
                )
            }
        }

        if (visibleEntries.isEmpty()) {
            item {
                EmptySectionCard(
                    message = when (uiState.selectedEntryFilter) {
                        EnergyEntryFilter.TODAY -> stringResource(id = R.string.no_today_check_ins)
                        EnergyEntryFilter.PREVIOUS -> stringResource(id = R.string.no_previous_check_ins)
                    },
                )
            }
        } else {
            itemsIndexed(
                items = visibleEntries,
                key = { _, entry -> entry.id },
            ) { index: Int, entry: EnergyEntry ->
                val isLatestTodayEntry =
                    uiState.selectedEntryFilter == EnergyEntryFilter.TODAY && index == 0
                EnergyEntryRow(
                    entry = entry,
                    timestampText = when (uiState.selectedEntryFilter) {
                        EnergyEntryFilter.TODAY -> formatTime(entry.timestamp)
                        EnergyEntryFilter.PREVIOUS -> formatDateTime(entry.timestamp)
                    },
                    isLatestTodayEntry = isLatestTodayEntry,
                    expanded = uiState.expandedEntryId == entry.id,
                    onToggleExpanded = { onToggleExpandedEntry(entry.id) },
                    onEditClick = { onEditEnergyClick(entry) },
                )
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
                    ?.let { mode ->
                        uiState.todayEntries.firstOrNull { it.id == mode.entryId }
                            ?: uiState.previousEntries.firstOrNull { it.id == mode.entryId }
                    }
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
private fun EntryFilterSelector(
    selectedFilter: EnergyEntryFilter,
    onEntryFilterChange: (EnergyEntryFilter) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = selectedFilter == EnergyEntryFilter.TODAY,
            onClick = { onEntryFilterChange(EnergyEntryFilter.TODAY) },
            shape = SegmentedButtonDefaults.itemShape(
                index = 0,
                count = 2,
            ),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                activeContentColor = MaterialTheme.colorScheme.onSurface,
                inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                activeBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                inactiveBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            ),
            modifier = Modifier.testTag(EnergyMapTestTags.TODAY_FILTER_BUTTON),
        ) {
            Text(text = stringResource(id = R.string.today_filter_label))
        }
        SegmentedButton(
            selected = selectedFilter == EnergyEntryFilter.PREVIOUS,
            onClick = { onEntryFilterChange(EnergyEntryFilter.PREVIOUS) },
            shape = SegmentedButtonDefaults.itemShape(
                index = 1,
                count = 2,
            ),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                activeContentColor = MaterialTheme.colorScheme.onSurface,
                inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                activeBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                inactiveBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            ),
            modifier = Modifier.testTag(EnergyMapTestTags.PREVIOUS_FILTER_BUTTON),
        ) {
            Text(text = stringResource(id = R.string.previous_filter_label))
        }
    }
}

@Composable
private fun EmptySectionCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        ),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun EnergyEntryRow(
    entry: EnergyEntry,
    timestampText: String,
    isLatestTodayEntry: Boolean,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onEditClick: () -> Unit,
) {
    val editEntryContentDescription = stringResource(id = R.string.edit_entry_content_description)
    val isExpandable = isExpandableNote(entry.note)
    val rowPadding = if (isLatestTodayEntry) 14.dp else 12.dp
    val rowSpacing = if (isLatestTodayEntry) 14.dp else 12.dp
    val scoreCircleSize = if (isLatestTodayEntry) 52.dp else 46.dp
    val scoreTextStyle =
        if (isLatestTodayEntry) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall
    val noteText = when {
        entry.note.isBlank() -> null
        expanded || !isExpandable -> entry.note
        else -> truncatedNotePreview(entry.note)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isLatestTodayEntry) Modifier.testTag(EnergyMapTestTags.LATEST_TODAY_ENTRY) else Modifier,
            ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${EnergyMapTestTags.ENERGY_ENTRY_PREFIX}${entry.id}"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.96f),
            ),
            shape = RoundedCornerShape(if (isLatestTodayEntry) 22.dp else 20.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isLatestTodayEntry) 0.12f else 0.08f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = rowPadding, vertical = rowPadding - 1.dp),
                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                verticalAlignment = Alignment.Top,
            ) {
                ScoreCircle(
                    score = entry.energyLevel,
                    circleColor = energyScoreColor(entry.energyLevel),
                    size = scoreCircleSize,
                    textStyle = scoreTextStyle,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = timestampText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(if (isLatestTodayEntry) 1f else 0.92f),
                    )
                    noteText?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .testTag("${EnergyMapTestTags.ENERGY_ENTRY_NOTE_PREFIX}${entry.id}")
                                .then(
                                    if (isExpandable) {
                                        Modifier.clickable(onClick = onToggleExpanded)
                                    } else {
                                        Modifier
                                    },
                                ),
                        )
                    }
                }
                FilledTonalIconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("${EnergyMapTestTags.ENERGY_ENTRY_EDIT_PREFIX}${entry.id}")
                        .semantics {
                            contentDescription = editEntryContentDescription
                        },
                    colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_media_next),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreCircle(
    score: Int,
    circleColor: Color,
    size: androidx.compose.ui.unit.Dp,
    textStyle: androidx.compose.ui.text.TextStyle,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(circleColor)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.16f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = score.toString(),
            style = textStyle,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
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
                        text = editorState.level.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Slider(
                        value = editorState.level.toFloat(),
                        onValueChange = { onLevelChange(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.testTag(EnergyMapTestTags.ENERGY_SLIDER),
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
