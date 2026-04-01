package com.hefengbao.kmp.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hefengbao.kmp.demo.di.jvmModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun main() = application {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(jvmModule)
        }
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "KmpDemo",
    ) {
        App()
    }
}