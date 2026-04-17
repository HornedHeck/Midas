@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.category

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.hornedheck.midas.ui.category.delete.DeleteCategoryScreen
import com.hornedheck.midas.ui.category.delete.DeleteCategoryViewModel
import com.hornedheck.midas.ui.category.edit.EditCategoryScreen
import com.hornedheck.midas.ui.category.edit.EditCategoryViewModel
import com.hornedheck.midas.ui.navigation.LocalNavBackStack
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.plugin.module.dsl.buildViewModel

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

    buildViewModel(EditCategoryViewModel::class) { (id: Long?) -> EditCategoryViewModel(id, get()) }
    buildViewModel(DeleteCategoryViewModel::class) { (id: Long) -> DeleteCategoryViewModel(id, get()) }

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
        /* View Here */
    }
    navigation<Category.RuleEdit> {
        /* View Here */
    }
}

