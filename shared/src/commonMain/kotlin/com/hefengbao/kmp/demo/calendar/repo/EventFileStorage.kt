package com.hefengbao.kmp.demo.calendar.repo

/**
 * Platform-specific abstraction for reading and writing the raw JSON that backs
 * [FileBackedEventRepository].  Each platform provides its own implementation.
 */
interface EventFileStorage {
    /** Read the full JSON string previously written by [writeAll], or null if nothing exists yet. */
    fun readAll(): String?

    /** Atomically persist [content] so that a subsequent [readAll] will return the same string. */
    fun writeAll(content: String)
}
