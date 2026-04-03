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

val uiModule = module {
    includes(
        authModule,
        mainModule,
        transactionModule,
        categoryModule,
        csvImportModule,
    )

    single {
        SavedStateConfiguration {
            serializersModule = getAll<SerializersModule>()
                .reduce { acc, module -> acc + module }
        }
    }
}

