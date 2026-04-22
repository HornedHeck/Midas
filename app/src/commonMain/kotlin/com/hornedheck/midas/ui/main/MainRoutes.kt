@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.main

import androidx.navigation3.runtime.NavKey
import com.hornedheck.midas.ui.navigation.LocalNavBackStack
import com.hornedheck.midas.ui.category.Category
import com.hornedheck.midas.ui.category.list.CategoriesListScreen
import com.hornedheck.midas.ui.category.list.CategoriesListViewModel
import com.hornedheck.midas.ui.transaction.Transaction
import com.hornedheck.midas.ui.transaction.list.TransactionListScreen
import com.hornedheck.midas.ui.transaction.list.TransactionListViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.viewModel

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

    viewModel<TransactionListViewModel>()
    navigation<Main.TransactionsList> {
        val backStack = LocalNavBackStack.current
        TransactionListScreen(
            onAddTransaction = { backStack.add(Transaction.Add()) },
            onTransactionClick = { id -> backStack.add(Transaction.Detail(id)) },
            onTransactionDelete = { id, description -> backStack.add(Transaction.Delete(id, description)) },
            onFilterClick = { backStack.add(Transaction.Filter) },
        )
    }
    viewModel<CategoriesListViewModel>()
    navigation<Main.CategoriesList> {
        val backStack = LocalNavBackStack.current
        CategoriesListScreen(
            onAddCategory = { backStack.add(Category.Edit()) },
            onItemClick = { id -> backStack.add(Category.Edit(id)) },
            onItemDelete = { id, name -> backStack.add(Category.Delete(id, name)) },
            onRulesClick = { backStack.add(Category.RulesList) },
        )
    }
    navigation<Main.Settings> {
        /* View Here */
    }
}


