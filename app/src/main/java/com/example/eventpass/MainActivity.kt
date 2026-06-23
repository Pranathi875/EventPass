package com.example.eventpass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.eventpass.ui.navigation.EventPassNavHost
import com.example.eventpass.ui.theme.EventPassTheme
import com.example.eventpass.util.AppViewModelFactory

/**
 * Single-activity host. Sets up the Compose theme and the navigation graph,
 * passing down a shared [AppViewModelFactory] so every screen's ViewModel can
 * reach the app-wide repository.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Build the factory once from the Application's repository.
        val factory = AppViewModelFactory(application as EventPassApplication)

        setContent {
            EventPassTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vmFactory = remember { factory }
                    EventPassNavHost(factory = vmFactory)
                }
            }
        }
    }
}
