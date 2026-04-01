package com.hefengbao.kmp.demo.di

import com.hefengbao.kmp.demo.IosEventFileStorage
import com.hefengbao.kmp.demo.calendar.repo.EventFileStorage
import com.hefengbao.kmp.demo.calendar.repo.EventRepository
import com.hefengbao.kmp.demo.calendar.repo.FileBackedEventRepository
import org.koin.dsl.module

val iosModule = module {
    single<EventFileStorage> { IosEventFileStorage() }
    single<EventRepository> { FileBackedEventRepository(get()) }
}
