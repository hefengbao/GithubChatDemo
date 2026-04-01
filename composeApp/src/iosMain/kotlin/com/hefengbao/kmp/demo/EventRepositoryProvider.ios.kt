package com.hefengbao.kmp.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hefengbao.kmp.demo.calendar.repo.EventRepository
import com.hefengbao.kmp.demo.calendar.repo.FileBackedEventRepository

@Composable
actual fun rememberEventRepository(): EventRepository =
    remember { FileBackedEventRepository(IosEventFileStorage()) }
