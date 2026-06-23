package com.example.eventpass.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the [Attendee] table.
 *
 * Read queries return [Flow] so the UI can observe live changes reactively
 * (Room emits a new list whenever the underlying table changes). Write
 * operations are suspend functions so they run off the main thread.
 */
@Dao
interface AttendeeDao {

    /** Observe every attendee, ordered alphabetically by name. */
    @Query("SELECT * FROM attendees ORDER BY name ASC")
    fun observeAll(): Flow<List<Attendee>>

    /**
     * Observe attendees whose name matches the given [query] (case-insensitive).
     * Pass an empty string to get everyone.
     */
    @Query("SELECT * FROM attendees WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<Attendee>>

    /** One-shot lookup of a single attendee by id, or null if not found. */
    @Query("SELECT * FROM attendees WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): Attendee?

    /** Total number of attendees registered. */
    @Query("SELECT COUNT(*) FROM attendees")
    fun observeTotalCount(): Flow<Int>

    /** Number of attendees that have already been checked in. */
    @Query("SELECT COUNT(*) FROM attendees WHERE checkedIn = 1")
    fun observeCheckedInCount(): Flow<Int>

    /** Insert or replace one attendee. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attendee: Attendee)

    /** Delete a single attendee. */
    @Delete
    suspend fun delete(attendee: Attendee)

    /** Delete an attendee by id. Returns rows removed (1 if it existed). */
    @Query("DELETE FROM attendees WHERE id = :id")
    suspend fun deleteById(id: String): Int

    /** Insert or replace several attendees (used for seeding). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(attendees: List<Attendee>)

    /**
     * Mark an attendee as checked in at [time]. Returns the number of rows
     * updated (1 if the attendee existed, 0 otherwise).
     */
    @Query("UPDATE attendees SET checkedIn = 1, checkInTime = :time WHERE id = :id")
    suspend fun markCheckedIn(id: String, time: Long): Int

    /** Convenience count used when deciding whether to seed sample data. */
    @Query("SELECT COUNT(*) FROM attendees")
    suspend fun count(): Int
}
