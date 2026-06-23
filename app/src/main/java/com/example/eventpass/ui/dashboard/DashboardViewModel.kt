package com.example.eventpass.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.repository.AttendeeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Aggregated counts shown on the dashboard.
 *
 * @property total     total registered attendees
 * @property checkedIn how many have checked in
 * @property remaining how many are still expected
 * @property isLoading true until the first values arrive from the database
 */
data class DashboardUiState(
    val total: Int = 0,
    val checkedIn: Int = 0,
    val remaining: Int = 0,
    val isLoading: Boolean = true
)

/**
 * Supplies live dashboard counts by combining the total and checked-in streams
 * coming from the repository. Exposed as a [StateFlow] for the Compose UI.
 */
class DashboardViewModel(
    repository: AttendeeRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        combine(
            repository.observeTotalCount(),
            repository.observeCheckedInCount()
        ) { total, checkedIn ->
            DashboardUiState(
                total = total,
                checkedIn = checkedIn,
                remaining = (total - checkedIn).coerceAtLeast(0),
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )
}
