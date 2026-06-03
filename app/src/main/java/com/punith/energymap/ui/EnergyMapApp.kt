package com.punith.energymap.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.punith.energymap.R
import com.punith.energymap.data.ActivityEntry
import com.punith.energymap.data.EnergyEntry
import java.time.LocalTime

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
            onShowActivityView = viewModel::onShowActivityView,
            onShowCheckInsView = viewModel::onShowCheckInsView,
            onEditEnergyClick = viewModel::onEditEnergyClick,
            onEntryFilterChange = viewModel::onEntryFilterChange,
            onToggleExpandedEntry = viewModel::onToggleExpandedEntry,
            onEditorLevelChange = viewModel::onEditorLevelChange,
            onEditorNoteChange = viewModel::onEditorNoteChange,
            onSaveEnergy = viewModel::onSaveEnergy,
            onRequestDelete = viewModel::onRequestDelete,
            onConfirmDelete = viewModel::onConfirmDelete,
            onSelectPreviousActivityDate = viewModel::onSelectPreviousActivityDate,
            onSelectNextActivityDate = viewModel::onSelectNextActivityDate,
            onSelectTodayActivityDate = viewModel::onSelectTodayActivityDate,
            onOpenStartActivityDialog = viewModel::onOpenStartActivityDialog,
            onQuickStartTitleChange = viewModel::onQuickStartTitleChange,
            onQuickStartNoteChange = viewModel::onQuickStartNoteChange,
            onConfirmStartActivity = viewModel::onConfirmStartActivity,
            onEndCurrentActivity = viewModel::onEndCurrentActivity,
            onOpenManualActivityDialog = viewModel::onOpenManualActivityDialog,
            onEditActivityClick = viewModel::onEditActivityClick,
            onActivityTitleChange = viewModel::onActivityTitleChange,
            onActivityNoteChange = viewModel::onActivityNoteChange,
            onActivityTimeFieldClick = viewModel::onActivityTimeFieldClick,
            onActivityTimeSelected = viewModel::onActivityTimeSelected,
            onDismissActivityTimePicker = viewModel::onDismissActivityTimePicker,
            onEndsNextDayChange = viewModel::onEndsNextDayChange,
            onSaveActivity = viewModel::onSaveActivity,
            onRequestDeleteActivity = viewModel::onRequestDeleteActivity,
            onConfirmDeleteActivity = viewModel::onConfirmDeleteActivity,
            onDismissDialogs = viewModel::onDismissDialogs,
        )
    }
}

@Composable
fun EnergyMapScreen(
    uiState: EnergyMapUiState,
    onAddEnergyClick: () -> Unit,
    onShowActivityView: () -> Unit,
    onShowCheckInsView: () -> Unit,
    onEditEnergyClick: (EnergyEntry) -> Unit,
    onEntryFilterChange: (EnergyEntryFilter) -> Unit,
    onToggleExpandedEntry: (Long) -> Unit,
    onEditorLevelChange: (Int) -> Unit,
    onEditorNoteChange: (String) -> Unit,
    onSaveEnergy: () -> Unit,
    onRequestDelete: (EnergyEntry) -> Unit,
    onConfirmDelete: () -> Unit,
    onSelectPreviousActivityDate: () -> Unit,
    onSelectNextActivityDate: () -> Unit,
    onSelectTodayActivityDate: () -> Unit,
    onOpenStartActivityDialog: () -> Unit,
    onQuickStartTitleChange: (String) -> Unit,
    onQuickStartNoteChange: (String) -> Unit,
    onConfirmStartActivity: () -> Unit,
    onEndCurrentActivity: () -> Unit,
    onOpenManualActivityDialog: () -> Unit,
    onEditActivityClick: (ActivityEntry) -> Unit,
    onActivityTitleChange: (String) -> Unit,
    onActivityNoteChange: (String) -> Unit,
    onActivityTimeFieldClick: (ActivityTimeField) -> Unit,
    onActivityTimeSelected: (LocalTime) -> Unit,
    onDismissActivityTimePicker: () -> Unit,
    onEndsNextDayChange: (Boolean) -> Unit,
    onSaveActivity: () -> Unit,
    onRequestDeleteActivity: (ActivityEntry) -> Unit,
    onConfirmDeleteActivity: () -> Unit,
    onDismissDialogs: () -> Unit,
) {
    when (uiState.currentView) {
        EnergyMapView.CheckIns -> CheckInsScreen(
            uiState = uiState,
            onAddEnergyClick = onAddEnergyClick,
            onShowActivityView = onShowActivityView,
            onEditEnergyClick = onEditEnergyClick,
            onEntryFilterChange = onEntryFilterChange,
            onToggleExpandedEntry = onToggleExpandedEntry,
        )

        EnergyMapView.ActivityTimeline -> ActivityTimelineScreen(
            uiState = uiState,
            onShowCheckInsView = onShowCheckInsView,
            onSelectPreviousActivityDate = onSelectPreviousActivityDate,
            onSelectNextActivityDate = onSelectNextActivityDate,
            onSelectTodayActivityDate = onSelectTodayActivityDate,
            onOpenStartActivityDialog = onOpenStartActivityDialog,
            onEndCurrentActivity = onEndCurrentActivity,
            onOpenManualActivityDialog = onOpenManualActivityDialog,
            onEditActivityClick = onEditActivityClick,
        )
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
                if (editingEntry != null) onRequestDelete(editingEntry)
            },
            onDismiss = onDismissDialogs,
        )
    }

    if (uiState.quickStartActivityState.isVisible) {
        QuickStartActivityDialog(
            state = uiState.quickStartActivityState,
            onTitleChange = onQuickStartTitleChange,
            onNoteChange = onQuickStartNoteChange,
            onStart = onConfirmStartActivity,
            onDismiss = onDismissDialogs,
        )
    }

    if (uiState.activityEditorState.isVisible) {
        ActivityEditorDialog(
            editorState = uiState.activityEditorState,
            onTitleChange = onActivityTitleChange,
            onNoteChange = onActivityNoteChange,
            onTimeFieldClick = onActivityTimeFieldClick,
            onEndsNextDayChange = onEndsNextDayChange,
            onSave = onSaveActivity,
            onDelete = {
                val editingEntry = (uiState.activityEditorState.mode as? ActivityEditorMode.Edit)
                    ?.let { mode -> uiState.activityEntries.firstOrNull { it.id == mode.entryId } }
                if (editingEntry != null) onRequestDeleteActivity(editingEntry)
            },
            onDismiss = onDismissDialogs,
        )
    }

    uiState.activityEditorState.timePickerField?.let { field ->
        ActivityTimePickerDialog(
            field = field,
            initialTime = when (field) {
                ActivityTimeField.Start -> uiState.activityEditorState.startTime
                ActivityTimeField.End -> uiState.activityEditorState.endTime
            },
            onConfirm = onActivityTimeSelected,
            onDismiss = onDismissActivityTimePicker,
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

    uiState.pendingDeleteActivityEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = onDismissDialogs,
            title = { Text(text = stringResource(id = R.string.delete_activity_dialog_title)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.delete_activity_dialog_message,
                        entry.title,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDeleteActivity,
                    modifier = Modifier.testTag(EnergyMapTestTags.ACTIVITY_DELETE_CONFIRM_BUTTON),
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
private fun CheckInsScreen(
    uiState: EnergyMapUiState,
    onAddEnergyClick: () -> Unit,
    onShowActivityView: () -> Unit,
    onEditEnergyClick: (EnergyEntry) -> Unit,
    onEntryFilterChange: (EnergyEntryFilter) -> Unit,
    onToggleExpandedEntry: (Long) -> Unit,
) {
    val visibleEntries = when (uiState.selectedEntryFilter) {
        EnergyEntryFilter.TODAY -> uiState.todayEntries
        EnergyEntryFilter.PREVIOUS -> uiState.previousEntries
    }
    val addEnergyContentDescription = stringResource(id = R.string.add_energy_content_description)
    val activityViewContentDescription = stringResource(id = R.string.activity_view_content_description)

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
                    Row(
                        modifier = Modifier.testTag(EnergyMapTestTags.ENERGY_CHECK_INS_HEADER_ACTION),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HeaderIconButton(
                            onClick = onShowActivityView,
                            iconRes = android.R.drawable.ic_menu_my_calendar,
                            contentDescription = activityViewContentDescription,
                            testTag = EnergyMapTestTags.ACTIVITY_VIEW_BUTTON,
                        )
                        FilledTonalIconButton(
                            onClick = onAddEnergyClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag(EnergyMapTestTags.ADD_CHECK_IN_BUTTON)
                                .semantics { contentDescription = addEnergyContentDescription },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
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
            ) { index, entry ->
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

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun ActivityTimelineScreen(
    uiState: EnergyMapUiState,
    onShowCheckInsView: () -> Unit,
    onSelectPreviousActivityDate: () -> Unit,
    onSelectNextActivityDate: () -> Unit,
    onSelectTodayActivityDate: () -> Unit,
    onOpenStartActivityDialog: () -> Unit,
    onEndCurrentActivity: () -> Unit,
    onOpenManualActivityDialog: () -> Unit,
    onEditActivityClick: (ActivityEntry) -> Unit,
) {
    val scrollState = rememberScrollState()
    val backContentDescription = stringResource(id = R.string.back_to_check_ins_content_description)
    val isTodaySelected = uiState.selectedActivityDate == localDateAt(System.currentTimeMillis(), java.time.ZoneId.systemDefault())
    val canGoNext = uiState.selectedActivityDate < localDateAt(System.currentTimeMillis(), java.time.ZoneId.systemDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .testTag(EnergyMapTestTags.ACTIVITY_TIMELINE_SCREEN),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                HeaderIconButton(
                    onClick = onShowCheckInsView,
                    iconRes = android.R.drawable.ic_media_previous,
                    contentDescription = backContentDescription,
                    testTag = EnergyMapTestTags.BACK_TO_CHECK_INS_BUTTON,
                )
                Text(
                    text = stringResource(id = R.string.activity_timeline_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                HeaderIconButton(
                    onClick = onSelectPreviousActivityDate,
                    iconRes = android.R.drawable.ic_media_previous,
                    contentDescription = stringResource(id = R.string.previous_day_content_description),
                    testTag = EnergyMapTestTags.PREVIOUS_DAY_BUTTON,
                )
                Text(
                    text = formatActivityDate(uiState.selectedActivityDate),
                    modifier = Modifier.testTag(EnergyMapTestTags.SELECTED_ACTIVITY_DATE),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                HeaderIconButton(
                    onClick = onSelectNextActivityDate,
                    enabled = canGoNext,
                    iconRes = android.R.drawable.ic_media_next,
                    contentDescription = stringResource(id = R.string.next_day_content_description),
                    testTag = EnergyMapTestTags.NEXT_DAY_BUTTON,
                )
            }
            TextButton(
                onClick = onSelectTodayActivityDate,
                modifier = Modifier.testTag(EnergyMapTestTags.TODAY_ACTIVITY_BUTTON),
            ) {
                Text(text = stringResource(id = R.string.today_filter_label))
            }
        }

        if (uiState.currentActivity != null) {
            Text(
                text = stringResource(
                    id = R.string.current_activity_status,
                    uiState.currentActivity.title,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isTodaySelected) {
                if (uiState.currentActivity == null) {
                    FilledTonalButton(
                        onClick = onOpenStartActivityDialog,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(EnergyMapTestTags.START_ACTIVITY_BUTTON),
                    ) {
                        Text(text = stringResource(id = R.string.start_activity_action))
                    }
                } else {
                    FilledTonalButton(
                        onClick = onEndCurrentActivity,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(EnergyMapTestTags.END_CURRENT_ACTIVITY_BUTTON),
                    ) {
                        Text(text = stringResource(id = R.string.end_current_activity_action))
                    }
                }
            }

            Button(
                onClick = onOpenManualActivityDialog,
                modifier = Modifier
                    .then(if (isTodaySelected) Modifier.weight(1f) else Modifier.fillMaxWidth())
                    .testTag(EnergyMapTestTags.MANUAL_ACTIVITY_BUTTON),
            ) {
                Text(text = stringResource(id = R.string.manual_activity_action))
            }
        }

        uiState.activityValidationMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag(EnergyMapTestTags.ACTIVITY_VALIDATION_MESSAGE),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
        ) {
            DailyTimeline(
                items = uiState.dailyTimelineItems,
                onEditActivityClick = onEditActivityClick,
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    iconRes: Int,
    contentDescription: String,
    testTag: String,
    enabled: Boolean = true,
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(38.dp)
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
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
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            colors = segmentedButtonColors(),
            modifier = Modifier.testTag(EnergyMapTestTags.TODAY_FILTER_BUTTON),
        ) {
            Text(text = stringResource(id = R.string.today_filter_label))
        }
        SegmentedButton(
            selected = selectedFilter == EnergyEntryFilter.PREVIOUS,
            onClick = { onEntryFilterChange(EnergyEntryFilter.PREVIOUS) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = segmentedButtonColors(),
            modifier = Modifier.testTag(EnergyMapTestTags.PREVIOUS_FILTER_BUTTON),
        ) {
            Text(text = stringResource(id = R.string.previous_filter_label))
        }
    }
}

@Composable
private fun segmentedButtonColors() = SegmentedButtonDefaults.colors(
    activeContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    activeContentColor = MaterialTheme.colorScheme.onSurface,
    inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    activeBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
    inactiveBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
)

@Composable
private fun EmptySectionCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isLatestTodayEntry) Modifier.testTag(EnergyMapTestTags.LATEST_TODAY_ENTRY) else Modifier)
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
                            .then(if (isExpandable) Modifier.clickable(onClick = onToggleExpanded) else Modifier),
                    )
                }
            }
            FilledTonalIconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("${EnergyMapTestTags.ENERGY_ENTRY_EDIT_PREFIX}${entry.id}")
                    .semantics { contentDescription = editEntryContentDescription },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_edit),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ScoreCircle(
    score: Int,
    circleColor: Color,
    size: Dp,
    textStyle: androidx.compose.ui.text.TextStyle,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(circleColor)
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.16f), shape = CircleShape),
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
private fun DailyTimeline(
    items: List<DailyTimelineItem>,
    onEditActivityClick: (ActivityEntry) -> Unit,
) {
    val activityBlocks = items.filterIsInstance<DailyTimelineItem.ActivityBlock>()
    val energyMarkers = items.filterIsInstance<DailyTimelineItem.EnergyMarker>()
    val timelineHeight = GRID_ROW_HEIGHT * 48

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(timelineHeight),
    ) {
        Column(
            modifier = Modifier.width(42.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            repeat(24) { hour ->
                Box(
                    modifier = Modifier.height(GRID_ROW_HEIGHT * 2),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Text(
                        text = hourLabel(hour),
                        style = TextStyle(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(18.dp),
                ),
        ) {
            TimelineGrid()

            activityBlocks.forEach { block ->
                ActivityBlockCard(
                    block = block,
                    onClick = { onEditActivityClick(block.entry) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 8.dp)
                        .offset(y = timelineHeight * block.topFraction)
                        .height((timelineHeight * block.heightFraction).coerceAtLeast(10.dp)),
                )
            }
        }

        Box(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight(),
        ) {
            energyMarkers.forEach { marker ->
                Box(
                    modifier = Modifier
                        .offset(y = (timelineHeight * marker.topFraction) - 14.dp)
                        .align(Alignment.TopCenter)
                        .testTag("${EnergyMapTestTags.ENERGY_TIMELINE_MARKER_PREFIX}${marker.entry.id}"),
                ) {
                    ScoreCircle(
                        score = marker.entry.energyLevel,
                        circleColor = marker.scoreColor,
                        size = 28.dp,
                        textStyle = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineGrid() {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(48) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GRID_ROW_HEIGHT),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineAlpha = if (index % 2 == 0) 0.12f else 0.06f
                    drawLine(
                        color = Color.White.copy(alpha = lineAlpha),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = if (index % 2 == 0) 1.8f else 1f,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityBlockCard(
    block: DailyTimelineItem.ActivityBlock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fullLabel = buildString {
        append(block.title)
        if (block.isOngoing) append(" • ")
        if (block.isOngoing) append(stringResource(id = R.string.ongoing_label))
    }

    Card(
        modifier = modifier
            .testTag("${EnergyMapTestTags.ACTIVITY_BLOCK_PREFIX}${block.entry.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        ),
    ) {
        when (block.contentMode) {
            ActivityBlockContentMode.Full -> {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = fullLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Text(
                        text = block.timeRangeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    block.notePreview?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                        )
                    }
                }
            }

            ActivityBlockContentMode.Compact -> {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = block.title,
                        style = TextStyle(fontSize = 11.sp),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Text(
                        text = block.timeRangeText,
                        style = TextStyle(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            ActivityBlockContentMode.TimeOnly -> {
                Box(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
                    Text(
                        text = block.timeRangeText,
                        style = TextStyle(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            ActivityBlockContentMode.None -> Unit
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

@Composable
private fun QuickStartActivityDialog(
    state: QuickStartActivityState,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onStart: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EnergyMapTestTags.QUICK_START_DIALOG),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.start_activity_action),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EnergyMapTestTags.ACTIVITY_TITLE_FIELD),
                    label = { Text(text = stringResource(id = R.string.activity_title_label)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.note,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EnergyMapTestTags.ACTIVITY_NOTE_FIELD),
                    label = { Text(text = stringResource(id = R.string.activity_note_label)) },
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                state.validationMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag(EnergyMapTestTags.ACTIVITY_VALIDATION_MESSAGE),
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.cancel_action))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onStart,
                        modifier = Modifier.testTag(EnergyMapTestTags.ACTIVITY_SAVE_BUTTON),
                    ) {
                        Text(text = stringResource(id = R.string.start_activity_action))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityEditorDialog(
    editorState: ActivityEditorState,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTimeFieldClick: (ActivityTimeField) -> Unit,
    onEndsNextDayChange: (Boolean) -> Unit,
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
                .testTag(EnergyMapTestTags.ACTIVITY_EDITOR_DIALOG),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = when (editorState.mode) {
                        ActivityEditorMode.AddManual -> stringResource(id = R.string.new_activity_title)
                        is ActivityEditorMode.Edit -> stringResource(id = R.string.edit_activity_title)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = editorState.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EnergyMapTestTags.ACTIVITY_TITLE_FIELD),
                    label = { Text(text = stringResource(id = R.string.activity_title_label)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = editorState.note,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EnergyMapTestTags.ACTIVITY_NOTE_FIELD),
                    label = { Text(text = stringResource(id = R.string.activity_note_label)) },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onTimeFieldClick(ActivityTimeField.Start) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag(EnergyMapTestTags.ACTIVITY_START_TIME_BUTTON),
                    ) {
                        Text(text = stringResource(id = R.string.start_time_value, formatLocalTime(editorState.startTime)))
                    }
                    Button(
                        onClick = { onTimeFieldClick(ActivityTimeField.End) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag(EnergyMapTestTags.ACTIVITY_END_TIME_BUTTON),
                    ) {
                        Text(text = stringResource(id = R.string.end_time_value, formatLocalTime(editorState.endTime)))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EnergyMapTestTags.ACTIVITY_ENDS_NEXT_DAY_TOGGLE),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = editorState.endsNextDay,
                        onCheckedChange = { onEndsNextDayChange(it) },
                    )
                    Text(text = stringResource(id = R.string.ends_next_day_label))
                }
                editorState.validationMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag(EnergyMapTestTags.ACTIVITY_VALIDATION_MESSAGE),
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (editorState.mode is ActivityEditorMode.Edit) {
                        TextButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag(EnergyMapTestTags.ACTIVITY_DELETE_BUTTON),
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
                        modifier = Modifier.testTag(EnergyMapTestTags.ACTIVITY_SAVE_BUTTON),
                    ) {
                        Text(text = stringResource(id = R.string.save_action))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityTimePickerDialog(
    field: ActivityTimeField,
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false,
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EnergyMapTestTags.ACTIVITY_TIME_PICKER_DIALOG),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = when (field) {
                        ActivityTimeField.Start -> stringResource(id = R.string.start_time_label)
                        ActivityTimeField.End -> stringResource(id = R.string.end_time_label)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                TimeInput(state = pickerState)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.cancel_action))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(LocalTime.of(pickerState.hour, pickerState.minute))
                        },
                    ) {
                        Text(text = stringResource(id = R.string.save_action))
                    }
                }
            }
        }
    }
}

private fun hourLabel(hour: Int): String = when {
    hour == 0 -> "12 AM"
    hour < 12 -> "$hour AM"
    hour == 12 -> "12 PM"
    else -> "${hour - 12} PM"
}

private operator fun Dp.times(factor: Int): Dp = (value * factor).dp
private operator fun Dp.times(factor: Float): Dp = (value * factor).dp

private val GRID_ROW_HEIGHT = 44.dp
