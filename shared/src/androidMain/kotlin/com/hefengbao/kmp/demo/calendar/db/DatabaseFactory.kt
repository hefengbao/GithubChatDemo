package com.hefengbao.kmp.demo.calendar.db

import android.content.Context
import androidx.room.Room

/**
 * Creates or returns the singleton Room [AppDatabase] instance for Android.
 * Thread-safe via double-checked locking; call from any thread.
 */
object DatabaseFactory {
    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context = context.applicationContext,
                klass = AppDatabase::class.java,
                name = "kmp-demo-calendar.db",
            ).build().also { instance = it }
        }
}

/** Convenience top-level function for use in platform-specific code. */
fun createRoomDatabase(context: Context): AppDatabase =
    DatabaseFactory.getDatabase(context)
