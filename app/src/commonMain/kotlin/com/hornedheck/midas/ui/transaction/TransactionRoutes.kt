@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.transaction

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Serializable
sealed interface Transaction : NavKey {

    @Serializable
    data class Add(val id: Long? = null) : Transaction

    @Serializable
    data class Detail(val id: Long) : Transaction
}

val transactionModule = module {
    single(named<Transaction>()) {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<Transaction>()
            }
        }
    }

    navigation<Transaction.Add> {
        /* View Here */
    }
    navigation<Transaction.Detail> {
        /* View Here */
    }
}

