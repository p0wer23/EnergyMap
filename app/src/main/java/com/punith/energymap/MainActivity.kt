package com.punith.energymap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.punith.energymap.ui.EnergyMapApp
import com.punith.energymap.ui.EnergyMapViewModel
import com.punith.energymap.ui.theme.EnergyMapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnergyMapTheme {
                val viewModel: EnergyMapViewModel = viewModel(factory = EnergyMapViewModel.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                EnergyMapApp(
                    state = state,
                    onAddSampleData = viewModel::seedSampleData,
                )
            }
        }
    }
}
