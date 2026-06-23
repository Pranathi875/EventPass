package com.example.eventpass.util

import org.json.JSONException
import org.json.JSONObject

/**
 * Helpers for interpreting the text contained inside a scanned QR code.
 *
 * EventPass QR codes can be either:
 *  - a plain attendee id, e.g.  "101", or
 *  - a small JSON object, e.g.  {"id":"101","name":"Aarav Sharma"}
 *
 * Either way we only need the attendee id to perform the check-in.
 */
object QrPayload {

    /**
     * Extracts the attendee id from a raw QR string.
     *
     * @return the trimmed id, or null if the payload is blank/unparseable.
     */
    fun extractAttendeeId(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val text = raw.trim()

        // Try to parse JSON first (payload looks like an object).
        if (text.startsWith("{")) {
            return try {
                val json = JSONObject(text)
                json.optString("id").takeIf { it.isNotBlank() }
            } catch (e: JSONException) {
                null
            }
        }

        // Otherwise treat the whole string as the id.
        return text
    }

    /**
     * Builds the canonical JSON payload we encode into a generated QR code.
     * Example: {"id":"101","name":"Aarav Sharma"}
     */
    fun buildPayload(id: String, name: String): String {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
        }.toString()
    }
}
