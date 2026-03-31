package com.hefengbao.kmp.demo.calendar.repo

import com.hefengbao.kmp.demo.calendar.domain.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory implementation of [EventRepository].
 * Used on iOS and JVM Desktop targets where Room is not available.
 * State is not persisted across app restarts.
 */
class InMemoryEventRepository : EventRepository {

    private val events = MutableStateFlow<Map<String, Event>>(emptyMap())

    override suspend fun upsert(event: Event) {
        events.update { it + (event.id to event) }
    }

    override suspend fun softDelete(id: String) {
        events.update { map ->
            val current = map[id] ?: return@update map
            map + (id to current.copy(deleted = true, dirty = true))
        }
    }

    override fun observeInRange(rangeStart: Long, rangeEnd: Long): Flow<List<Event>> =
        events.map { map ->
            map.values
                .filter { event ->
                    !event.deleted &&
                        event.startAtEpochMillis >= rangeStart &&
                        event.startAtEpochMillis < rangeEnd
                }
                .sortedBy { it.startAtEpochMillis }
        }

    override suspend fun getById(id: String): Event? =
        events.value[id]?.takeIf { !it.deleted }

    override suspend fun listDirty(): List<Event> =
        events.value.values.filter { it.dirty && !it.deleted }

    override suspend fun markClean(id: String) {
        events.update { map ->
            val current = map[id] ?: return@update map
            map + (id to current.copy(dirty = false))
        }
    }
}
