package com.hornedheck.midas

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
