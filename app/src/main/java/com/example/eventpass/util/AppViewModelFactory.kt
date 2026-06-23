package com.example.eventpass.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eventpass.EventPassApplication
import com.example.eventpass.ui.addattendee.AddAttendeeViewModel
import com.example.eventpass.ui.attendees.AttendeeListViewModel
import com.example.eventpass.ui.dashboard.DashboardViewModel
import com.example.eventpass.ui.login.LoginViewModel
import com.example.eventpass.ui.scanner.ScannerViewModel
import com.example.eventpass.ui.stats.StatsViewModel

/**
 * A tiny manual dependency-injection factory.
 *
 * Rather than pulling in Hilt/Dagger for a college project, we construct each
 * ViewModel here and hand it the shared [AttendeeRepository] from the
 * [EventPassApplication]. This keeps the wiring explicit and easy to follow.
 */
class AppViewModelFactory(
    private val app: EventPassApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = app.repository
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel() as T

            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repo) as T

            modelClass.isAssignableFrom(ScannerViewModel::class.java) ->
                ScannerViewModel(repo) as T

            modelClass.isAssignableFrom(AttendeeListViewModel::class.java) ->
                AttendeeListViewModel(repo) as T

            modelClass.isAssignableFrom(AddAttendeeViewModel::class.java) ->
                AddAttendeeViewModel(repo) as T

            modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                StatsViewModel(repo) as T

            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
