package com.hefengbao.kmp.demo.calendar.repo

import com.hefengbao.kmp.demo.calendar.domain.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * File-backed implementation of [EventRepository] for iOS and JVM Desktop targets.
 *
 * All events are serialised to a single JSON array via [EventFileStorage] and reloaded
 * when the repository is first created (i.e., on app start), providing persistence
 * across restarts without requiring Room / SQLite.
 *
 * A [MutableStateFlow] keeps the in-memory map in sync so that all [observeInRange]
 * collectors see updates reactively.
 */
class FileBackedEventRepository(private val storage: EventFileStorage) : EventRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val _events = MutableStateFlow<Map<String, Event>>(emptyMap())

    init {
        val raw = storage.readAll()
        if (!raw.isNullOrBlank()) {
            try {
                val list = json.decodeFromString<List<Event>>(raw)
                _events.value = list.associateBy { it.id }
            } catch (_: Exception) {
                // Corrupt or incompatible file — start fresh
            }
        }
    }

    private fun persist() {
        storage.writeAll(json.encodeToString(_events.value.values.toList()))
    }

    override suspend fun upsert(event: Event) {
        _events.update { it + (event.id to event) }
        persist()
    }

    override suspend fun softDelete(id: String) {
        _events.update { map ->
            val current = map[id] ?: return@update map
            map + (id to current.copy(deleted = true, dirty = true))
        }
        persist()
    }

    override fun observeInRange(rangeStart: Long, rangeEnd: Long): Flow<List<Event>> =
        _events.map { map ->
            map.values
                .filter { event ->
                    !event.deleted &&
                        event.startAtEpochMillis >= rangeStart &&
                        event.startAtEpochMillis < rangeEnd
                }
                .sortedBy { it.startAtEpochMillis }
        }

    override suspend fun getById(id: String): Event? =
        _events.value[id]?.takeIf { !it.deleted }

    override suspend fun listDirty(): List<Event> =
        _events.value.values.filter { it.dirty && !it.deleted }

    override suspend fun markClean(id: String) {
        _events.update { map ->
            val current = map[id] ?: return@update map
            map + (id to current.copy(dirty = false))
        }
        persist()
    }
}
