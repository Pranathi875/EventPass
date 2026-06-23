package com.example.eventpass.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single event attendee stored in the Room database.
 *
 * Each attendee is uniquely identified by [id] which is the same value encoded
 * inside their QR code. When they are scanned at the door we flip [checkedIn] to
 * true and record the moment in [checkInTime].
 *
 * @property id          Unique attendee id (also the QR payload, e.g. "101").
 * @property name        Full name of the attendee.
 * @property email       Contact email.
 * @property ticketType  Ticket tier, e.g. "General", "VIP", "Speaker".
 * @property checkedIn   Whether the attendee has been checked in at the event.
 * @property checkInTime Epoch millis of the check-in, or null if not yet checked in.
 */
@Entity(tableName = "attendees")
data class Attendee(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val ticketType: String,
    val checkedIn: Boolean = false,
    val checkInTime: Long? = null
)
