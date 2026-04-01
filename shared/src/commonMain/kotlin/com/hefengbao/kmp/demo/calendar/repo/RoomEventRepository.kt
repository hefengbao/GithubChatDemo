package com.hefengbao.kmp.demo.calendar.repo

import com.hefengbao.kmp.demo.calendar.db.EventDao
import com.hefengbao.kmp.demo.calendar.db.EventEntity
import com.hefengbao.kmp.demo.calendar.domain.Event
import com.hefengbao.kmp.demo.calendar.domain.RecurrenceRule
import com.hefengbao.kmp.demo.calendar.domain.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room-backed implementation of [EventRepository] for Android and JVM Desktop.
 * Converts between [EventEntity] (persistence layer) and [Event] (domain layer).
 *
 * Recurrence rules and reminders are stored as JSON strings for this iteration.
 * Future iterations may introduce dedicated tables and TypeConverters.
 */
class RoomEventRepository(private val dao: EventDao) : EventRepository {

    override suspend fun upsert(event: Event) {
        dao.upsert(event.toEntity())
    }

    override suspend fun softDelete(id: String) {
        dao.softDelete(id, updatedAt = Clock.System.now().toEpochMilliseconds())
    }

    override fun observeInRange(rangeStart: Long, rangeEnd: Long): Flow<List<Event>> =
        dao.observeInRange(rangeStart, rangeEnd).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getById(id: String): Event? =
        dao.getById(id)?.toDomain()

    override suspend fun listDirty(): List<Event> =
        dao.listDirty().map { it.toDomain() }

    override suspend fun markClean(id: String) {
        dao.markClean(id)
    }
}

// ── Mapping helpers ─────────────────────────────────────────────────────────

private fun Event.toEntity(): EventEntity = EventEntity(
    id = id,
    title = title,
    description = description,
    startAtEpochMillis = startAtEpochMillis,
    endAtEpochMillis = endAtEpochMillis,
    allDay = allDay,
    timezone = timezone,
    recurrenceJson = recurrence?.let { Json.encodeToString(it) },
    remindersJson = reminders.takeIf { it.isNotEmpty() }?.let { Json.encodeToString(it) },
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    deleted = deleted,
    dirty = dirty,
    serverVersion = serverVersion,
)

private fun EventEntity.toDomain(): Event = Event(
    id = id,
    title = title,
    description = description,
    startAtEpochMillis = startAtEpochMillis,
    endAtEpochMillis = endAtEpochMillis,
    allDay = allDay,
    timezone = timezone,
    recurrence = recurrenceJson?.let { runCatching { Json.decodeFromString<RecurrenceRule>(it) }.getOrNull() },
    reminders = remindersJson?.let { runCatching { Json.decodeFromString<List<Reminder>>(it) }.getOrElse { emptyList() } } ?: emptyList(),
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    deleted = deleted,
    dirty = dirty,
    serverVersion = serverVersion,
)
