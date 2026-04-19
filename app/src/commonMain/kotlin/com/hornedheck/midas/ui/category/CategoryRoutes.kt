@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.category

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.hornedheck.midas.ui.category.delete.DeleteCategoryScreen
import com.hornedheck.midas.ui.category.delete.DeleteCategoryViewModel
import com.hornedheck.midas.ui.category.edit.EditCategoryScreen
import com.hornedheck.midas.ui.category.edit.EditCategoryViewModel
import com.hornedheck.midas.ui.navigation.LocalNavBackStack
import com.hornedheck.midas.ui.rules.edit.EditRuleScreen
import com.hornedheck.midas.ui.rules.edit.EditRuleViewModel
import com.hornedheck.midas.ui.rules.list.RulesListScreen
import com.hornedheck.midas.ui.rules.list.RulesListViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.viewModel

@Serializable
sealed interface Category : NavKey {

    @Serializable
    data class Edit(val id: Long? = null) : Category

    @Serializable
    data class Delete(val id: Long, val name: String) : Category

    @Serializable
    data object RulesList : Category

    @Serializable
    data class RuleEdit(val id: Long? = null) : Category
}

val categoryModule = module {
    single(named<Category>()) {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclassesOfSealed<Category>()
            }
        }
    }

    // TODO koin skill
    viewModel<EditCategoryViewModel>()
    viewModel<DeleteCategoryViewModel>()
    viewModel<RulesListViewModel>()
    viewModel<EditRuleViewModel>()

    navigation<Category.Edit>(metadata = DialogSceneStrategy.dialog()) { key ->
        val backStack = LocalNavBackStack.current
        EditCategoryScreen(
            categoryId = key.id,
            onDismiss = { backStack.removeLastOrNull() },
            onSaved = { backStack.removeLastOrNull() },
        )
    }

    navigation<Category.Delete>(metadata = DialogSceneStrategy.dialog()) { key ->
        val backStack = LocalNavBackStack.current
        DeleteCategoryScreen(
            categoryId = key.id,
            name = key.name,
            onDismiss = { backStack.removeLastOrNull() },
            onDeleted = { backStack.removeLastOrNull() },
        )
    }

    navigation<Category.RulesList> {
        val backStack = LocalNavBackStack.current
        RulesListScreen(
            onBack = { backStack.removeLastOrNull() },
            onAddRule = { backStack.add(Category.RuleEdit()) },
            onRuleClick = { id -> backStack.add(Category.RuleEdit(id)) },
        )
    }

    navigation<Category.RuleEdit> { key ->
        val backStack = LocalNavBackStack.current
        EditRuleScreen(
            ruleId = key.id,
            onBack = { backStack.removeLastOrNull() },
            onSaved = { backStack.removeLastOrNull() },
        )
    }
}


