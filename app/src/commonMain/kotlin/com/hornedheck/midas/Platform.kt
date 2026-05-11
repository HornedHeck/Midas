package com.hornedheck.midas
import kotlinx.coroutines.CoroutineDispatcher

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect val appVersion: String

expect val isSystemDarkModeSupported: Boolean

