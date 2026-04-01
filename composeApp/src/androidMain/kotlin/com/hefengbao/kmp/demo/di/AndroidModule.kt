package com.hefengbao.kmp.demo.di

import com.hefengbao.kmp.demo.calendar.db.AppDatabase
import com.hefengbao.kmp.demo.calendar.db.createRoomDatabase
import com.hefengbao.kmp.demo.calendar.repo.EventRepository
import com.hefengbao.kmp.demo.calendar.repo.RoomEventRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val androidModule = module {
    single { createRoomDatabase(androidApplication()) }
    single<EventRepository> { RoomEventRepository(get<AppDatabase>().eventDao()) }
}
