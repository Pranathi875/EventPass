package com.example.eventpass.ui.attendees

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventpass.data.local.Attendee
import com.example.eventpass.ui.components.EmptyState
import com.example.eventpass.ui.components.LoadingState
import com.example.eventpass.util.DateFormatter

/**
 * Attendee list with a search bar. Each row shows the attendee's name, ticket
 * type, and check-in status (with timestamp when checked in). Handles loading
 * and empty states gracefully.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendeeListScreen(
    viewModel: AttendeeListViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendees") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar filters by name.
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search by name") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            when {
                state.isLoading -> LoadingState()

                state.attendees.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Group,
                    title = if (query.isBlank()) "No attendees yet" else "No matches",
                    subtitle = if (query.isBlank())
                        "Add attendees to get started."
                    else
                        "No attendee names contain \"$query\"."
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.attendees, key = { it.id }) { attendee ->
                        AttendeeRow(
                            attendee = attendee,
                            onDelete = { viewModel.deleteAttendee(attendee) }
                        )
                    }
                }
            }
        }
    }
}

/** A single attendee card row. */
@Composable
private fun AttendeeRow(
    attendee: Attendee,
    onDelete: () -> Unit
) {
    // Local state for the "are you sure?" confirmation dialog.
    var showConfirm by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status icon (green check vs. grey circle).
            Icon(
                imageVector = if (attendee.checkedIn)
                    Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (attendee.checkedIn)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = attendee.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "#${attendee.id} • ${attendee.ticketType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (attendee.checkedIn) {
                    Text(
                        text = "Checked in: ${DateFormatter.format(attendee.checkInTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Delete (remove attendee) action.
            IconButton(onClick = { showConfirm = true }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove ${attendee.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Confirmation dialog so deletes aren't accidental.
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Remove attendee?") },
            text = { Text("Remove ${attendee.name} (#${attendee.id}) from the event? This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
