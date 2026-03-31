package com.hefengbao.kmp.demo.calendar.repo

import com.hefengbao.kmp.demo.calendar.domain.Event
import kotlinx.coroutines.flow.Flow

/**
 * Contract for calendar event persistence.
 * Android uses a Room-backed implementation; other platforms use [InMemoryEventRepository].
 */
interface EventRepository {

    /**
     * Insert or update an event. If an event with the same [Event.id] already exists,
     * it is replaced.
     */
    suspend fun upsert(event: Event)

    /**
     * Mark an event as deleted without removing it from storage (tombstone for sync).
     * The event is hidden from all non-sync queries after this call.
     */
    suspend fun softDelete(id: String)

    /**
     * Observe non-deleted events whose [Event.startAtEpochMillis] falls in [rangeStart, rangeEnd).
     * Emits a new sorted list whenever the underlying data changes.
     */
    fun observeInRange(rangeStart: Long, rangeEnd: Long): Flow<List<Event>>

    /** Return a single non-deleted event by ID, or null if not found. */
    suspend fun getById(id: String): Event?

    /** Return all non-deleted events that have local-only changes not yet synced to the server. */
    suspend fun listDirty(): List<Event>

    /** Clear the dirty flag for an event after a successful server sync. */
    suspend fun markClean(id: String)
}
