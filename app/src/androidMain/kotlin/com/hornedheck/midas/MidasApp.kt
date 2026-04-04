package com.hornedheck.midas

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MidasApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val koin = startKoin {
            androidContext(this@MidasApp)
            modules(modules)
        }
    }
}
