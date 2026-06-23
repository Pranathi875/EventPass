package com.example.eventpass.ui.attendees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpass.data.local.Attendee
import com.example.eventpass.data.repository.AttendeeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State for the attendee list screen.
 *
 * @property attendees the (possibly filtered) list to render
 * @property isLoading true until the first DB emission arrives
 */
data class AttendeeListUiState(
    val attendees: List<Attendee> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * Exposes a searchable, live list of attendees. The [query] StateFlow drives a
 * [flatMapLatest] so each keystroke swaps to a fresh filtered DB query.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AttendeeListViewModel(
    private val repository: AttendeeRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<AttendeeListUiState> =
        _query
            .flatMapLatest { q -> repository.search(q.trim()) }
            .map { list -> AttendeeListUiState(attendees = list, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AttendeeListUiState()
            )

    /** Update the search query; the list updates reactively. */
    fun onQueryChange(value: String) {
        _query.value = value
    }

    /** Remove an attendee from the event. The list updates reactively. */
    fun deleteAttendee(attendee: Attendee) {
        viewModelScope.launch {
            repository.deleteAttendee(attendee)
        }
    }
}
