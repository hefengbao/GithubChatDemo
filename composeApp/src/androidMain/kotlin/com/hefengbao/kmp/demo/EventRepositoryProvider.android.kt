package com.hefengbao.kmp.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.hefengbao.kmp.demo.calendar.db.createRoomDatabase
import com.hefengbao.kmp.demo.calendar.repo.EventRepository
import com.hefengbao.kmp.demo.calendar.repo.RoomEventRepository

@Composable
actual fun rememberEventRepository(): EventRepository {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        RoomEventRepository(createRoomDatabase(context).eventDao())
    }
}
