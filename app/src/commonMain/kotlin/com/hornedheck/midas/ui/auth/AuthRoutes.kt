@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.auth

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Serializable
sealed interface Auth : NavKey {

    @Serializable
    data object SignIn : Auth

    @Serializable
    data object SignUp : Auth

}

val authModule = module {
    single(named<Auth>()) {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<Auth>()
            }
        }
    }

    navigation<Auth.SignIn> {
        /* View Here */
    }
    navigation<Auth.SignUp> {
        /* View Here */
    }
}
