package com.hornedheck.midas

interface Platform {
    val name: String
}

val appVersion: String = BuildKonfig.APP_VERSION

expect val isSystemDarkModeSupported: Boolean

