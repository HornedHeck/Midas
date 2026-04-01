package com.hornedheck.midas

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(modules)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Midas",
    ) {
        App()
    }
}