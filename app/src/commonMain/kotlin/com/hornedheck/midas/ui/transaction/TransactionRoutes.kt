@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.transaction

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.hornedheck.midas.ui.navigation.LocalNavBackStack
import com.hornedheck.midas.ui.transaction.add.AddTransactionScreen
import com.hornedheck.midas.ui.transaction.add.AddTransactionViewModel
import com.hornedheck.midas.ui.transaction.delete.DeleteTransactionScreen
import com.hornedheck.midas.ui.transaction.delete.DeleteTransactionViewModel
import com.hornedheck.midas.ui.transaction.detail.TransactionDetailScreen
import com.hornedheck.midas.ui.transaction.detail.TransactionDetailViewModel
import com.hornedheck.midas.ui.transaction.filter.TransactionFilterScreen
import com.hornedheck.midas.ui.transaction.filter.TransactionFilterViewModel
import com.hornedheck.midas.domain.usecase.TransactionsListUseCase
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel

@Serializable
sealed interface Transaction : NavKey {

    @Serializable
    data class Add(val id: Long? = null) : Transaction

    @Serializable
    data class Detail(val id: Long) : Transaction

    @Serializable
    data class Delete(val id: Long, val description: String) : Transaction

    @Serializable
    data object Filter : Transaction
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
    viewModel<DeleteTransactionViewModel>()
    single<TransactionsListUseCase>()
    viewModel<TransactionFilterViewModel>()

    navigation<Transaction.Add> {
        val backStack = LocalNavBackStack.current
        AddTransactionScreen(
            transactionId = it.id,
            onBack = { backStack.removeLastOrNull() },
        )
    }

    navigation<Transaction.Detail> { key ->
        val backStack = LocalNavBackStack.current
        TransactionDetailScreen(
            transactionId = key.id,
            onBack = { backStack.removeLastOrNull() },
            onEdit = { backStack.add(Transaction.Add(id = key.id)) },
            onDelete = { description -> backStack.add(Transaction.Delete(key.id, description)) },
        )
    }

    navigation<Transaction.Delete>(metadata = DialogSceneStrategy.dialog()) { key ->
        val backStack = LocalNavBackStack.current
        DeleteTransactionScreen(
            transactionId = key.id,
            description = key.description,
            onDismiss = { backStack.removeLastOrNull() },
            onDeleted = {
                backStack.removeLastOrNull()
                if (backStack.lastOrNull() is Transaction.Detail) {
                    backStack.removeLastOrNull()
                }
            },
        )
    }

    navigation<Transaction.Filter> {
        val backStack = LocalNavBackStack.current
        TransactionFilterScreen(
            onBack = { backStack.removeLastOrNull() },
        )
    }
}

