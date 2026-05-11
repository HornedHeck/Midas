package com.hornedheck.midas

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect val appVersion: String

expect val isSystemDarkModeSupported: Boolean

