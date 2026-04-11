@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.transaction

import androidx.navigation3.runtime.NavKey
import com.hornedheck.midas.ui.navigation.LocalNavBackStack
import com.hornedheck.midas.ui.transaction.add.AddTransactionScreen
import com.hornedheck.midas.ui.transaction.add.AddTransactionViewModel
import com.hornedheck.midas.ui.transaction.detail.TransactionDetailScreen
import com.hornedheck.midas.ui.transaction.detail.TransactionDetailViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.viewModel

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

    viewModel<AddTransactionViewModel>()
    viewModel<TransactionDetailViewModel>()

    navigation<Transaction.Add> {
        val backStack = LocalNavBackStack.current
        AddTransactionScreen(
            transactionId = it.id,
            onBack = { backStack.removeLastOrNull() },
        )
    }

    navigation<Transaction.Detail> {
        val backStack = LocalNavBackStack.current
        val transactionId = it.id
        TransactionDetailScreen(
            transactionId = transactionId,
            onBack = { backStack.removeLastOrNull() },
            onEdit = { backStack.add(Transaction.Add(id = transactionId)) },
        )
    }
}

