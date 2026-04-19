package com.hornedheck.midas.data.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val dataStoreModule: Module = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath {
            val dir = File(System.getProperty("user.home"), ".midas")
            dir.mkdirs()
            dir.resolve("midas_rules.preferences_pb").absolutePath.toPath()
        }
    }
}
