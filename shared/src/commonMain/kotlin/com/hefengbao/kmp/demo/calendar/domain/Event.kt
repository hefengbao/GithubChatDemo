package com.hefengbao.kmp.demo.calendar.domain

import kotlinx.serialization.Serializable

/**
 * Domain model representing a calendar event.
 * Contains cloud-sync redundancy fields (createdAt/updatedAt, deleted, dirty, serverVersion).
 */
data class Event(
    val id: String,
    val title: String,
    val description: String = "",
    val startAtEpochMillis: Long,
    val endAtEpochMillis: Long,
    val allDay: Boolean = false,
    val timezone: String = "UTC",
    val recurrence: RecurrenceRule? = null,
    val reminders: List<Reminder> = emptyList(),
    // Cloud sync fields
    val createdAtEpochMillis: Long = 0L,
    val updatedAtEpochMillis: Long = 0L,
    val deleted: Boolean = false,
    val dirty: Boolean = true,
    val serverVersion: Long? = null,
)
