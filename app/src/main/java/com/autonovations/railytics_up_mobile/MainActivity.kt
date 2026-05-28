package com.autonovations.railytics_up_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.autonovations.railytics_up_mobile.ui.screens.MainScreen
import com.autonovations.railytics_up_mobile.ui.theme.RailyticsUPmobileTheme
import com.autonovations.railytics_up_mobile.ui.viewmodel.RailyticsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[RailyticsViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            RailyticsUPmobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0A05)
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}