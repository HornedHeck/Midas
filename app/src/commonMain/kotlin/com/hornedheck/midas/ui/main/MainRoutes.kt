@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.main

import androidx.navigation3.runtime.NavKey
import com.hornedheck.midas.ui.navigation.LocalNavBackStack
import com.hornedheck.midas.ui.category.Category
import com.hornedheck.midas.ui.category.list.CategoriesListScreen
import com.hornedheck.midas.ui.category.list.CategoriesListViewModel
import com.hornedheck.midas.ui.home.HomeScreen
import com.hornedheck.midas.ui.home.HomeViewModel
import com.hornedheck.midas.ui.settings.SettingsScreen
import com.hornedheck.midas.ui.settings.SettingsViewModel
import com.hornedheck.midas.ui.transaction.Transaction
import com.hornedheck.midas.ui.transaction.list.TransactionListScreen
import com.hornedheck.midas.ui.transaction.list.TransactionListViewModel
import com.hornedheck.midas.domain.model.transaction.TransactionFilter
import com.hornedheck.midas.domain.model.transaction.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.viewModel

@Serializable
sealed interface Main : NavKey {

    @Serializable
    data object Dashboard : Main

    @Serializable
    data class TransactionsList(
        val initialCategoryId: Long? = null,
        val initialDateFrom: LocalDate? = null,
        val initialDateTo: LocalDate? = null,
    ) : Main

    @Serializable
    data object CategoriesList : Main

    @Serializable
    data object Settings : Main
}

val mainModule = module {
    viewModel<HomeViewModel>()

    single(named<Main>()) {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<Main>()
            }
        }
    }

    navigation<Main.Dashboard> {
        val backStack = LocalNavBackStack.current
        HomeScreen(
            onAddTransaction = { backStack.add(Transaction.Add()) },
            onCategoryClick = { categoryId, _, dateFrom, dateTo ->
                backStack.add(Main.TransactionsList(categoryId, dateFrom, dateTo))
            },
        )
    }

    viewModel<TransactionListViewModel>()
    navigation<Main.TransactionsList> { key ->
        val backStack = LocalNavBackStack.current
        val initialFilter = if (key.initialDateFrom != null && key.initialDateTo != null) {
            TransactionFilter(
                type = TransactionType.EXPENSES,
                dateFrom = key.initialDateFrom,
                dateTo = key.initialDateTo,
                categoryIds = setOf(key.initialCategoryId),
            )
        } else null
        TransactionListScreen(
            initialFilter = initialFilter,
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
    viewModel<SettingsViewModel>()
    navigation<Main.Settings> {
        SettingsScreen()
    }
}


