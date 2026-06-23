package com.example.eventpass.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.repository.AttendeeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Check-in progress summary.
 *
 * @property total      total attendees
 * @property checkedIn  checked-in attendees
 * @property remaining  not-yet-arrived attendees
 * @property progress   fraction in 0f..1f for the progress bar
 * @property isLoading  true until first emission
 */
data class StatsUiState(
    val total: Int = 0,
    val checkedIn: Int = 0,
    val remaining: Int = 0,
    val progress: Float = 0f,
    val isLoading: Boolean = true
)

/** Computes live check-in progress for the stats screen. */
class StatsViewModel(
    repository: AttendeeRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> =
        combine(
            repository.observeTotalCount(),
            repository.observeCheckedInCount()
        ) { total, checkedIn ->
            StatsUiState(
                total = total,
                checkedIn = checkedIn,
                remaining = (total - checkedIn).coerceAtLeast(0),
                progress = if (total > 0) checkedIn.toFloat() / total else 0f,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState()
        )
}
