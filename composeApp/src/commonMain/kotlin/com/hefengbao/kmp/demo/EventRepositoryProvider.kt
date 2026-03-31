package com.hefengbao.kmp.demo

import androidx.compose.runtime.Composable
import com.hefengbao.kmp.demo.calendar.repo.EventRepository

/**
 * Returns a platform-appropriate [EventRepository] instance:
 * - Android: Room-backed, persisted to SQLite.
 * - iOS / JVM Desktop: in-memory (no persistence).
 */
@Composable
expect fun rememberEventRepository(): EventRepository
