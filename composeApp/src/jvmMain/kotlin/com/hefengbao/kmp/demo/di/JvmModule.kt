package com.hefengbao.kmp.demo.di

import com.hefengbao.kmp.demo.JvmEventFileStorage
import com.hefengbao.kmp.demo.calendar.repo.EventFileStorage
import com.hefengbao.kmp.demo.calendar.repo.EventRepository
import com.hefengbao.kmp.demo.calendar.repo.FileBackedEventRepository
import org.koin.dsl.module

val jvmModule = module {
    single<EventFileStorage> { JvmEventFileStorage() }
    single<EventRepository> { FileBackedEventRepository(get()) }
}
