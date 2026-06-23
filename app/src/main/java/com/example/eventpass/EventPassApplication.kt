package com.example.eventpass

import android.app.Application
import com.example.eventpass.data.local.EventPassDatabase
import com.example.eventpass.data.repository.AttendeeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Custom [Application] that owns the app-wide singletons: the Room database and
 * the [AttendeeRepository]. ViewModels obtain the repository from here (via
 * AppViewModelFactory), giving us simple manual dependency injection.
 */
class EventPassApplication : Application() {

    /** Application-scoped coroutine scope, used for one-off work like DB seeding. */
    private val applicationScope = CoroutineScope(SupervisorJob())

    /** Lazily created database so it's only built when first needed. */
    private val database: EventPassDatabase by lazy {
        EventPassDatabase.getInstance(this, applicationScope)
    }

    /** The single repository instance shared across the whole app. */
    val repository: AttendeeRepository by lazy {
        AttendeeRepository(database.attendeeDao())
    }
}
