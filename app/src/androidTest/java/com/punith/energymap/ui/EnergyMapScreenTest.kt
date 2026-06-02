package com.punith.energymap.ui

import androidx.compose.runtime.getValue
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

    @Test
    fun addEditAndDeleteEnergyCheckInFlowWorks() {
        composeRule.setContent {
            EnergyMapTheme {
                var entries by remember { mutableStateOf(emptyList<EnergyEntry>()) }
                var editorState by remember { mutableStateOf(EnergyEditorState()) }
                var pendingDeleteEntry by remember { mutableStateOf<EnergyEntry?>(null) }
                var nextId by remember { mutableStateOf(1L) }

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
                        previousDaySections = derivedState.previousDaySections,
                        editorState = editorState,
                        pendingDeleteEntry = pendingDeleteEntry,
                    ),
                    onAddEnergyClick = {
                        val latestToday = derivedState.todayEntries.firstOrNull()
                        editorState = EnergyEditorState(
                            isVisible = true,
                            mode = EnergyEditorMode.Add,
                            level = latestToday?.energyLevel ?: 5,
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

        composeRule.onNodeWithTag(EnergyMapTestTags.ADD_CHECK_IN_BUTTON).performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_NOTE_FIELD).performTextInput("Morning check-in")
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_SLIDER)
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                setProgress(8f)
            }
        composeRule.onNodeWithTag(EnergyMapTestTags.SAVE_CHECK_IN_BUTTON).performClick()
        composeRule.onNodeWithText("8/10").assertIsDisplayed()

        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_PREFIX}1").performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_NOTE_FIELD).performTextClearance()
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_NOTE_FIELD).performTextInput("Updated check-in")
        composeRule.onNodeWithTag(EnergyMapTestTags.ENERGY_SLIDER)
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                setProgress(6f)
            }
        composeRule.onNodeWithTag(EnergyMapTestTags.SAVE_CHECK_IN_BUTTON).performClick()
        composeRule.onNodeWithText("6/10").assertIsDisplayed()
        composeRule.onNodeWithText("Updated check-in").assertIsDisplayed()

        composeRule.onNodeWithTag("${EnergyMapTestTags.ENERGY_ENTRY_PREFIX}1").performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.DELETE_CHECK_IN_BUTTON).performClick()
        composeRule.onNodeWithTag(EnergyMapTestTags.DELETE_CONFIRM_BUTTON).performClick()
        composeRule.onNodeWithText("No check-ins recorded for today.").assertIsDisplayed()
        composeRule.onAllNodesWithTag("${EnergyMapTestTags.ENERGY_ENTRY_PREFIX}1").assertCountEquals(0)
    }
}
