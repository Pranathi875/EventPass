package com.example.eventpass.ui.addattendee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.local.Attendee
import com.example.eventpass.data.repository.AttendeeRepository
import com.example.eventpass.util.QrPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State for the "Add attendee" form.
 *
 * @property name        name field text
 * @property email       email field text
 * @property ticketType  selected ticket type
 * @property error       validation error, if any
 * @property isSaving    true while the insert is in flight
 * @property created     the saved attendee once successful (drives the QR view)
 * @property qrContent   the JSON payload encoded into the generated QR code
 */
data class AddAttendeeUiState(
    val name: String = "",
    val email: String = "",
    val ticketType: String = TICKET_TYPES.first(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val created: Attendee? = null,
    val qrContent: String? = null
) {
    companion object {
        /** Available ticket tiers for the dropdown. */
        val TICKET_TYPES = listOf("General", "VIP", "Speaker", "Student")
    }
}

/**
 * Validates and saves a new attendee, then exposes the data needed to render a
 * scannable QR code for them.
 */
class AddAttendeeViewModel(
    private val repository: AttendeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAttendeeUiState())
    val uiState: StateFlow<AddAttendeeUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onTicketTypeChange(value: String) = _uiState.update { it.copy(ticketType = value) }

    /**
     * Validates the form, generates a unique id, persists the attendee, and
     * publishes the generated QR payload on success.
     */
    fun save() {
        val state = _uiState.value
        val name = state.name.trim()
        val email = state.email.trim()

        if (name.isEmpty()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        if (!isValidEmail(email)) {
            _uiState.update { it.copy(error = "Enter a valid email address") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val id = generateId()
                val attendee = Attendee(
                    id = id,
                    name = name,
                    email = email,
                    ticketType = state.ticketType
                )
                repository.addAttendee(attendee)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        created = attendee,
                        qrContent = QrPayload.buildPayload(id, name)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "Couldn't save: ${e.message}")
                }
            }
        }
    }

    /** Reset the form to add another attendee. */
    fun reset() {
        _uiState.value = AddAttendeeUiState()
    }

    /** Basic email validation using the platform pattern. */
    private fun isValidEmail(email: String): Boolean =
        email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    /** Generate a reasonably unique attendee id (timestamp-based). */
    private fun generateId(): String = "A" + (System.currentTimeMillis() % 1_000_000L).toString()
}
