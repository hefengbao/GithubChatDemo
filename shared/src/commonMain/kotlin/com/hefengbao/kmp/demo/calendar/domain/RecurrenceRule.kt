package com.hefengbao.kmp.demo.calendar.domain

import kotlinx.serialization.Serializable

/**
 * Recurrence rule for repeating events.
 * Serialized to/from JSON string in the database layer.
 */
@Serializable
data class RecurrenceRule(
    val frequency: Frequency,
    val interval: Int = 1,
    /** Repeat until this epoch-millis timestamp (inclusive), or null if not bounded by date. */
    val until: Long? = null,
    /** Maximum occurrence count, or null if unbounded. */
    val count: Int? = null,
) {
    enum class Frequency { DAILY, WEEKLY, MONTHLY, YEARLY }
}
