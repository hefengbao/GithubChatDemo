package com.hefengbao.kmp.demo.calendar.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for persisting calendar events on Android.
 *
 * Redundancy fields for future cloud sync:
 * - [createdAtEpochMillis] / [updatedAtEpochMillis]: audit timestamps
 * - [deleted]: soft-delete tombstone (true = hidden from queries)
 * - [dirty]: local-only change not yet synced to server
 * - [serverVersion]: server-side version counter for conflict resolution
 *
 * Complex objects ([recurrenceJson], [remindersJson]) are stored as JSON strings
 * to avoid additional join tables in this first iteration.
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val startAtEpochMillis: Long = 0L,
    val endAtEpochMillis: Long = 0L,
    val allDay: Boolean = false,
    val timezone: String = "UTC",
    /** Serialized [com.hefengbao.kmp.demo.calendar.domain.RecurrenceRule] or null. */
    val recurrenceJson: String? = null,
    /** Serialized list of [com.hefengbao.kmp.demo.calendar.domain.Reminder] or null. */
    val remindersJson: String? = null,
    // Cloud sync redundancy fields
    val createdAtEpochMillis: Long = 0L,
    val updatedAtEpochMillis: Long = 0L,
    val deleted: Boolean = false,
    val dirty: Boolean = true,
    val serverVersion: Long? = null,
)
