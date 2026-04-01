package com.hefengbao.kmp.demo

import androidx.compose.ui.window.ComposeUIViewController
import com.hefengbao.kmp.demo.di.iosModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoinIfNeeded()
    return ComposeUIViewController { App() }
}

private fun initKoinIfNeeded() {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(iosModule)
        }
    }
}
