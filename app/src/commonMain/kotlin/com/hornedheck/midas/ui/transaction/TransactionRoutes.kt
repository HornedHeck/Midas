@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.transaction

import androidx.navigation3.runtime.NavKey
import com.hornedheck.midas.ui.navigation.LocalNavBackStack
import com.hornedheck.midas.ui.transaction.add.AddTransactionScreen
import com.hornedheck.midas.ui.transaction.add.AddTransactionViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.plugin.module.dsl.viewModel

@Serializable
sealed interface Transaction : NavKey {

    @Serializable
    data class Add(val id: String? = null) : Transaction

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

    viewModel<AddTransactionViewModel>()
    navigation<Transaction.Add> {
        val backStack = LocalNavBackStack.current
        AddTransactionScreen(
            onBack = { backStack.removeLastOrNull<NavKey>() },
        )
    }
    navigation<Transaction.Detail> {
        /* View Here */
    }
}

