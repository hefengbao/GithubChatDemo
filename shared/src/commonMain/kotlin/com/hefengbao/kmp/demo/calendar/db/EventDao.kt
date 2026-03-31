package com.hefengbao.kmp.demo.calendar.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Upsert
    suspend fun upsert(event: EventEntity)

    @Query("UPDATE events SET deleted = 1, dirty = 1, updatedAtEpochMillis = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("SELECT * FROM events WHERE id = :id AND deleted = 0")
    suspend fun getById(id: String): EventEntity?

    /**
     * Observe events whose start time falls within [rangeStart, rangeEnd).
     * Excludes soft-deleted entries and emits a new list on every change.
     */
    @Query(
        "SELECT * FROM events " +
            "WHERE deleted = 0 " +
            "AND startAtEpochMillis >= :rangeStart " +
            "AND startAtEpochMillis < :rangeEnd " +
            "ORDER BY startAtEpochMillis ASC"
    )
    fun observeInRange(rangeStart: Long, rangeEnd: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE dirty = 1 AND deleted = 0")
    suspend fun listDirty(): List<EventEntity>

    @Query("UPDATE events SET dirty = 0 WHERE id = :id")
    suspend fun markClean(id: String)
}
