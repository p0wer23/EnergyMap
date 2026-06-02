package com.punith.energymap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EnergyMapApp(
    state: EnergyMapUiState,
    onAddSampleData: () -> Unit,
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("EnergyMap", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Compose, Room, Navigation-ready app shell for local energy and activity tracking.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Setup status", style = MaterialTheme.typography.titleMedium)
                        Text("Energy entries: ${state.energyEntries.size}")
                        Text("Activity entries: ${state.activityEntries.size}")
                        Button(onClick = onAddSampleData) {
                            Text("Insert sample records")
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Daily timeline", style = MaterialTheme.typography.titleMedium)
                    Text("${state.timelineItems.size} items", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (state.timelineItems.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No records yet", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Use the sample action to verify Room persistence and the initial Compose wiring.")
                        }
                    }
                }
            } else {
                items(state.timelineItems) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.subtitle, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
