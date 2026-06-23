package com.example.eventpass.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Dashboard: the home screen after login. Shows live check-in counts and the
 * primary actions (scan, view attendees, add attendee, stats).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartScanning: () -> Unit,
    onViewAttendees: () -> Unit,
    onAddAttendee: () -> Unit,
    onViewStats: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Event overview",
                style = MaterialTheme.typography.titleMedium
            )

            // Three summary stat cards in a row.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total",
                    value = state.total,
                    icon = Icons.Filled.Groups,
                    container = MaterialTheme.colorScheme.primaryContainer,
                    onContainer = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Checked in",
                    value = state.checkedIn,
                    icon = Icons.Filled.HowToReg,
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    onContainer = MaterialTheme.colorScheme.onTertiaryContainer
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Remaining",
                    value = state.remaining,
                    icon = Icons.Filled.Groups,
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    onContainer = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.height(8.dp))

            // Primary call to action.
            Button(
                onClick = onStartScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Start scanning")
            }

            OutlinedButton(
                onClick = onViewAttendees,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Groups, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("View attendees")
            }

            OutlinedButton(
                onClick = onAddAttendee,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Add attendee")
            }

            OutlinedButton(
                onClick = onViewStats,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.BarChart, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("View stats")
            }
        }
    }
}

/** A compact card showing a single labelled count with an icon. */
@Composable
private fun StatCard(
    label: String,
    value: Int,
    icon: ImageVector,
    container: Color,
    onContainer: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = onContainer)
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = onContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = onContainer
            )
        }
    }
}
