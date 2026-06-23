package com.example.eventpass.ui.addattendee

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventpass.util.QrCodeGenerator

/**
 * Add-attendee screen. Collects name/email/ticket type, saves the attendee, and
 * then shows a generated QR code that encodes their id + name.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAttendeeScreen(
    viewModel: AddAttendeeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add attendee") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val created = state.created
            if (created == null) {
                // ---------- FORM ----------
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Full name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                TicketTypeDropdown(
                    selected = state.ticketType,
                    onSelected = viewModel::onTicketTypeChange
                )

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save & generate QR")
                    }
                }
            } else {
                // ---------- SUCCESS + QR ----------
                Text(
                    text = "Attendee added 🎉",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${created.name} • #${created.id} • ${created.ticketType}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Generate the QR bitmap once for the current payload.
                val qrContent = state.qrContent.orEmpty()
                val qrBitmap = remember(qrContent) {
                    QrCodeGenerator.generateImageBitmap(qrContent, size = 600)
                }

                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "QR code for ${created.name}",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(240.dp)
                            .padding(top = 16.dp)
                    )
                } else {
                    Text(
                        text = "Couldn't generate QR code.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = "Scan this code at check-in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                OutlinedButton(
                    onClick = viewModel::reset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add another")
                }
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/** Simple exposed dropdown for picking a ticket type. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketTypeDropdown(
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ticket type") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select ticket type")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AddAttendeeUiState.TICKET_TYPES.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
