package com.hornedheck.midas.ui.csv

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Serializable
sealed interface CsvImport : NavKey {

    @Serializable
    data object FilePicker : CsvImport

    @Serializable
    data class Mapping(val fileUri: String) : CsvImport

    @Serializable
    data class Summary(val importId: Long) : CsvImport
}

val csvImportModule = module {
    single(named<CsvImport>()) {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<CsvImport>()
            }
        }
    }

    navigation<CsvImport.FilePicker> {
        /* View Here */
    }
    navigation<CsvImport.Mapping> {
        /* View Here */
    }
    navigation<CsvImport.Summary> {
        /* View Here */
    }
}

