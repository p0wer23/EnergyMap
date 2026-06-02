package com.punith.energymap.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.punith.energymap.data.EnergyEntry
import com.punith.energymap.ui.theme.EnergyMapTheme
import java.time.ZoneId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnergyMapScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val nowMillis: Long = 1_717_329_600_000
    private val zoneId: ZoneId = ZoneId.of("UTC")
    private val longNote = (1..21).joinToString(" ") { "word$it" }
    private val longNotePreview = (1..20).joinToString(" ") { "word$it" } + "..."

    @Test
    fun addEditExpandFilterAndDeleteEnergyCheckInFlowWorks() {
        composeRule.setContent {
            EnergyMapTheme {
                var entries by remember {
                    mutableStateOf(
                        listOf(
                            EnergyEntry(
                                id = 99,
                                timestamp = nowMillis - 86_400_000,
                                energyLevel = 4,
                                note = "Previous day note",
                            ),
                        ),
                    )
                }
                var editorState by remember { mutableStateOf(EnergyEditorState()) }
                var pendingDeleteEntry by remember { mutableStateOf<EnergyEntry?>(null) }
                var nextId by remember { mutableLongStateOf(1L) }
                var selectedFilter by remember { mutableStateOf(EnergyEntryFilter.TODAY) }
                var expandedEntryId by remember { mutableStateOf<Long?>(null) }

                fun dismissDialogs() {
                    editorState = EnergyEditorState()
                    pendingDeleteEntry = null
                }

                val derivedState = deriveEnergyState(
                    entries = entries,
                    nowMillis = nowMillis,
                    zoneId = zoneId,
                )

                EnergyMapScreen(
                    uiState = EnergyMapUiState(
                        currentEnergy = derivedState.currentEnergy,
                        latestOverallEntry = derivedState.latestOverallEntry,
                        hasCheckInToday = derivedState.todayEntries.isNotEmpty(),
                        todayEntries = derivedState.todayEntries,
                        previousEntries = derivedState.previousEntries,
                        selectedEntryFilter = selectedFilter,
                        expandedEntryId = expandedEntryId,
                        editorState = editorState,
                        pendingDeleteEntry = pendingDeleteEntry,
                    ),
                    onAddEnergyClick = {
                        editorState = EnergyEditorState(
                            isVisible = true,
                            mode = EnergyEditorMode.Add,
                            level = derivedState.latestOverallEntry?.energyLevel ?: 5,
                        )
                    },
                    onEditEnergyClick = { entry ->
                        editorState = EnergyEditorState(
                            isVisible = true,
                            mode = EnergyEditorMode.Edit(entry.id),
                            level = entry.energyLevel,
                            note = entry.note,
                            timestampText = formatDateTime(entry.timestamp, zoneId),
                        )
                    },
                    onEntryFilterChange = { filter ->
                        selectedFilter = filter
                    },
                    onToggleExpandedEntry = { entryId ->
                        expandedEntryId = if (expandedEntryId == entryId) null else entryId
                    },
                    onEditorLevelChange = { level ->
                        editorState = editorState.copy(level = level)
                    },
                    onEditorNoteChange = { note ->
                        editorState = editorState.copy(note = note)
                    },
                    onSaveEnergy = {
                        when (val mode = editorState.mode) {
                            EnergyEditorMode.Add -> {
                                entries = (entries + EnergyEntry(
                                    id = nextId,
                                    timestamp = nowMillis,
                                    energyLevel = editorState.level,
                                    note = editorState.note.trim(),
                                )).sortedByDescending(EnergyEntry::timestamp)
                                nextId += 1
                            }

                            is EnergyEditorMode.Edit -> {
                                entries = entries.map { entry ->
                                    if (entry.id == mode.entryId) {
                                        buildUpdatedEnergyEntry(
                                            existing = entry,
                                            newLevel = editorState.level,
                                            newNote = editorState.note.trim(),
                                        )
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
                        pendingDeleteEntry = entry
                        editorState = editorState.copy(isVisible = false)
                    },
                    onConfirmDelete = {
                        val deleteId = pendingDeleteEntry?.id
                        entries = entries.filterNot { it.id == deleteId }
                        dismissDialogs()
                    },
                    onDismissDialogs = ::dismissDialogs,
                )
            }
        }

        composeRule.onNodeWithTag(EnergyMapTestTags.TODAY_FILTER_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithText("No check-ins recorded for today.").assertIsDisplayed()

        composeRule.onNodeWithTag(EnergyMapTestTags.ADD_CHECK_IN_BUTTON).performClick()
        composeRule.onNodeWithText("4").assertIsDisplayed()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_NOTE_FIELD).performTextInput(longNote)
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_SLIDER)
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                setProgress(8f)
            }
        composeRule.onNodeWithTag(EnergyMapTestTags.SAVE_CHECK_IN_BUTTON).performClick()

        composeRule.onNodeWithText(longNotePreview).assertIsDisplayed()
        composeRule.onNodeWithText(longNote).assertDoesNotExist()

        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_NOTE_PREFIX}1").performClick()
        composeRule.onNodeWithText(longNote).assertIsDisplayed()

        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_NOTE_PREFIX}1").performClick()
        composeRule.onNodeWithText(longNotePreview).assertIsDisplayed()

        composeRule.onNodeWithTag(EnergyMapTestTags.PREVIOUS_FILTER_BUTTON).performClick()
        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_PREFIX}99").assertIsDisplayed()
        composeRule.onNodeWithText("Previous day note").assertIsDisplayed()
        composeRule.onNodeWithText(formatDateTime(nowMillis - 86_400_000, zoneId)).assertIsDisplayed()
        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_PREFIX}1").assertDoesNotExist()

        composeRule.onNodeWithTag(EnergyMapTestTags.TODAY_FILTER_BUTTON).performClick()
        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_EDIT_PREFIX}1").performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_NOTE_FIELD).performTextClearance()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_NOTE_FIELD).performTextInput("Updated check-in")
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_SLIDER)
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                setProgress(6f)
            }
        composeRule.onNodeWithTag(EnergyMapTestTags.SAVE_CHECK_IN_BUTTON).performClick()

        composeRule.onNodeWithText("Updated check-in").assertIsDisplayed()
        composeRule.onNodeWithText(longNotePreview).assertDoesNotExist()

        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_EDIT_PREFIX}1").performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.DELETE_CHECK_IN_BUTTON).performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.DELETE_CONFIRM_BUTTON).performClick()
        composeRule.onNodeWithText("No check-ins recorded for today.").assertIsDisplayed()
        composeRule.onAllNodesWithTag("${EnergyMapTestTags.ENERGY_ENTRY_PREFIX}1").assertCountEquals(0)
    }
}
