package com.hornedheck.midas.ui

import androidx.savedstate.serialization.SavedStateConfiguration
import com.hornedheck.midas.ui.auth.authModule
import com.hornedheck.midas.ui.category.categoryModule
import com.hornedheck.midas.ui.csv.csvImportModule
import com.hornedheck.midas.ui.main.mainModule
import com.hornedheck.midas.ui.transaction.transactionModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

val uiModule = module {
    includes(
        authModule,
        mainModule,
        transactionModule,
        categoryModule,
        csvImportModule,
    )

    single {
        create(::provideSavedStateConfig)
    }
}

fun provideSavedStateConfig(modules: List<SerializersModule>) =
    SavedStateConfiguration {
        serializersModule =
            modules.reduceOrNull { acc, module -> acc + module } ?: SerializersModule {}
    }
