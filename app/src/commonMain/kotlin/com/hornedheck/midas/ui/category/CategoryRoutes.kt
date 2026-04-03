package com.hornedheck.midas.ui.category

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Serializable
sealed interface Category : NavKey {

    @Serializable
    data class Edit(val id: Long? = null) : Category

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

    navigation<Category.Edit> {
        /* View Here */
    }
    navigation<Category.RulesList> {
        /* View Here */
    }
    navigation<Category.RuleEdit> {
        /* View Here */
    }
}

