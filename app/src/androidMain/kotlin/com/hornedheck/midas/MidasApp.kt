package com.hornedheck.midas

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MidasApp : Application() {
    override fun onCreate() {
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
        super.onCreate()

        initKoin {
            androidContext(this@MidasApp)
            androidLogger()
        }
    }
}
