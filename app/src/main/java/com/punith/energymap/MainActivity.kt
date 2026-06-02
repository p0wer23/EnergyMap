package com.punith.energymap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.punith.energymap.ui.EnergyMapApp
import com.punith.energymap.ui.theme.EnergyMapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnergyMapTheme {
                EnergyMapApp()
            }
        }
    }
}
