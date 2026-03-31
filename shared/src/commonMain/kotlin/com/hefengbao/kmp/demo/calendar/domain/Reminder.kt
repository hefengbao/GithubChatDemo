package com.hefengbao.kmp.demo.calendar.domain

import kotlinx.serialization.Serializable

/**
 * Event reminder configuration.
 * Serialized to/from JSON string in the database layer.
 */
@Serializable
data class Reminder(
    /** Number of minutes before the event start to show the reminder. */
    val minutesBefore: Int,
)
