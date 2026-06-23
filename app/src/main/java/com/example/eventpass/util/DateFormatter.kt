package com.example.eventpass.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Small helper to render check-in timestamps in a friendly way. */
object DateFormatter {

    private val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    /** Formats epoch millis like "23 Jun, 02:15 PM", or "—" when null. */
    fun format(epochMillis: Long?): String {
        if (epochMillis == null) return "—"
        return formatter.format(Date(epochMillis))
    }
}
