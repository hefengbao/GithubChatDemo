package com.hefengbao.kmp.demo.calendar

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** Format an epoch-millis timestamp as "YYYY-MM-DD HH:mm" in [tz]. */
internal fun formatMillis(millis: Long, tz: TimeZone): String {
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz)
    return formatLocalDateTime(dt)
}

/** Format a [LocalDateTime] as "YYYY-MM-DD HH:mm". */
internal fun formatLocalDateTime(dt: LocalDateTime): String =
    "${dt.year.pad(4)}-${(dt.month.ordinal + 1).pad()}-${dt.day.pad()} ${dt.hour.pad()}:${dt.minute.pad()}"

/** Parse "YYYY-MM-DD HH:mm" → epoch millis, or null on failure. */
internal fun parseMillisFromString(str: String, tz: TimeZone): Long? = try {
    val trimmed = str.trim()
    val spaceIdx = trimmed.indexOf(' ')
    if (spaceIdx < 0) return null
    val datePart = trimmed.substring(0, spaceIdx).split("-")
    val timePart = trimmed.substring(spaceIdx + 1).split(":")
    if (datePart.size < 3 || timePart.size < 2) return null
    LocalDateTime(
        year = datePart[0].toInt(),
        month = Month(datePart[1].toInt()),
        day = datePart[2].toInt(),
        hour = timePart[0].toInt(),
        minute = timePart[1].toInt(),
    ).toInstant(tz).toEpochMilliseconds()
} catch (_: Exception) {
    null
}

/** Parse "YYYY-MM-DD" or the date portion of "YYYY-MM-DD HH:mm" → [LocalDate], or null on failure. */
internal fun parseDateOnly(str: String): LocalDate? = try {
    val datePart = str.trim().substringBefore(' ').split("-")
    if (datePart.size < 3) null
    else LocalDate(datePart[0].toInt(), datePart[1].toInt(), datePart[2].toInt())
} catch (_: Exception) {
    null
}

internal fun Int.pad(length: Int = 2): String = toString().padStart(length, '0')
