package com.hefengbao.kmp.demo

import com.hefengbao.kmp.demo.calendar.repo.EventFileStorage
import java.io.File

/**
 * JVM Desktop implementation of [EventFileStorage].
 * Persists event JSON to a file in the user's home directory so that
 * data survives application restarts.
 */
internal class JvmEventFileStorage : EventFileStorage {

    private val file: File by lazy {
        File(System.getProperty("user.home"), ".kmp-demo-events.json")
    }

    override fun readAll(): String? =
        if (file.exists()) file.readText() else null

    override fun writeAll(content: String) {
        file.writeText(content)
    }
}
