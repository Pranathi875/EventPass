package com.example.eventpass.data.repository

import com.example.eventpass.data.local.Attendee
import com.example.eventpass.data.local.AttendeeDao
import kotlinx.coroutines.flow.Flow

/**
 * Result of attempting to check an attendee in via a QR scan.
 * Sealed so the UI can exhaustively react to every outcome.
 */
sealed interface CheckInResult {
    /** Attendee found and successfully checked in just now. */
    data class Success(val attendee: Attendee) : CheckInResult

    /** Attendee found but they were already checked in earlier. */
    data class AlreadyCheckedIn(val attendee: Attendee) : CheckInResult

    /** No attendee matched the scanned id. */
    data class NotFound(val scannedId: String) : CheckInResult
}

/**
 * Single source of truth for attendee data. The rest of the app (ViewModels)
 * talk to this repository instead of touching Room directly. This keeps the
 * MVVM layers cleanly separated and makes the data layer easy to swap or test.
 */
class AttendeeRepository(
    private val dao: AttendeeDao
) {
    /** Live stream of all attendees. */
    fun observeAll(): Flow<List<Attendee>> = dao.observeAll()

    /** Live, name-filtered stream of attendees. */
    fun search(query: String): Flow<List<Attendee>> = dao.search(query)

    /** Live total attendee count. */
    fun observeTotalCount(): Flow<Int> = dao.observeTotalCount()

    /** Live checked-in count. */
    fun observeCheckedInCount(): Flow<Int> = dao.observeCheckedInCount()

    /** Add (or overwrite) an attendee. */
    suspend fun addAttendee(attendee: Attendee) = dao.upsert(attendee)

    /** Remove an attendee (e.g. someone who cancelled). */
    suspend fun deleteAttendee(attendee: Attendee) = dao.delete(attendee)

    suspend fun findById(id: String): Attendee? = dao.findById(id)

    /**
     * Core check-in logic used by the scanner.
     *
     * 1. Look up the attendee by the scanned [id].
     * 2. If missing -> [CheckInResult.NotFound].
     * 3. If already checked in -> [CheckInResult.AlreadyCheckedIn].
     * 4. Otherwise stamp the current time and return [CheckInResult.Success].
     */
    suspend fun checkIn(id: String): CheckInResult {
        val attendee = dao.findById(id) ?: return CheckInResult.NotFound(id)
        if (attendee.checkedIn) {
            return CheckInResult.AlreadyCheckedIn(attendee)
        }
        val now = System.currentTimeMillis()
        dao.markCheckedIn(id, now)
        return CheckInResult.Success(attendee.copy(checkedIn = true, checkInTime = now))
    }
}
