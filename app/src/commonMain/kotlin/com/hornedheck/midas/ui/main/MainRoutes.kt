@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.main

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Serializable
sealed interface Main : NavKey {

    @Serializable
    data object Dashboard : Main

    @Serializable
    data object TransactionsList : Main

    @Serializable
    data object CategoriesList : Main

    @Serializable
    data object Settings : Main
}

val mainModule = module {
    single(named<Main>()) {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<Main>()
            }
        }
    }

    navigation<Main.Dashboard> {
        /* View Here */
    }
    navigation<Main.TransactionsList> {
        /* View Here */
    }
    navigation<Main.CategoriesList> {
        /* View Here */
    }
    navigation<Main.Settings> {
        /* View Here */
    }
}

