package com.example.eventpass.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.repository.AttendeeRepository
import com.example.eventpass.data.repository.CheckInResult
import com.example.eventpass.util.QrPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State for the scanner screen.
 *
 * @property isProcessing true while a scanned code is being looked up (used to
 *                        pause the analyzer so we don't process the same code
 *                        dozens of times per second).
 * @property result       outcome of the most recent scan, shown in a dialog.
 * @property error        unexpected error message, if any.
 */
data class ScannerUiState(
    val isProcessing: Boolean = false,
    val result: CheckInResult? = null,
    val error: String? = null
)

/**
 * Owns the check-in logic for scanned QR codes. The UI feeds raw QR strings in
 * via [onQrCodeScanned]; this ViewModel parses the id, performs the check-in
 * against the repository, and publishes the result for the dialog to display.
 */
class ScannerViewModel(
    private val repository: AttendeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    /**
     * Called by the ML Kit analyzer for every decoded QR string. We ignore new
     * codes while a previous one is still being processed or while a result
     * dialog is showing, so a single physical scan produces a single result.
     */
    fun onQrCodeScanned(raw: String) {
        val state = _uiState.value
        if (state.isProcessing || state.result != null) return

        val id = QrPayload.extractAttendeeId(raw)
        if (id == null) {
            _uiState.update { it.copy(error = "Couldn't read that QR code. Try again.") }
            return
        }

        _uiState.update { it.copy(isProcessing = true, error = null) }
        viewModelScope.launch {
            try {
                val result = repository.checkIn(id)
                _uiState.update { it.copy(isProcessing = false, result = result) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, error = "Check-in failed: ${e.message}")
                }
            }
        }
    }

    /** Dismiss the result dialog and resume scanning. */
    fun dismissResult() {
        _uiState.update { it.copy(result = null) }
    }

    /** Clear a transient error. */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
