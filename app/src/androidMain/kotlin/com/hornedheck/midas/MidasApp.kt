package com.hornedheck.midas

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MidasApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MidasApp)
            androidLogger()
        }
    }
}
