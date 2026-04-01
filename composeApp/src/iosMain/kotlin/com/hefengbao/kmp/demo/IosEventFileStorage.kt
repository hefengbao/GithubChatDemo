package com.hefengbao.kmp.demo

import com.hefengbao.kmp.demo.calendar.repo.EventFileStorage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

/**
 * iOS implementation of [EventFileStorage].
 * Persists event JSON to a file in the app's Documents directory so that
 * data survives application restarts.
 */
@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
internal class IosEventFileStorage : EventFileStorage {

    private val filePath: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            expandTilde = true,
        )
        val documentsDir = paths.first() as String
        "$documentsDir/kmp-demo-events.json"
    }

    override fun readAll(): String? =
        NSString.stringWithContentsOfFile(
            path = filePath,
            encoding = NSUTF8StringEncoding,
            error = null,
        )

    override fun writeAll(content: String) {
        NSString.create(string = content).writeToFile(
            path = filePath,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null,
        )
    }
}
