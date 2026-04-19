package com.hornedheck.midas.ui.category.delete

sealed interface DeleteCategoryState {
    data object Loading : DeleteCategoryState
    data class Confirm(val pendingRulesCount: Int) : DeleteCategoryState
    data object Deleting : DeleteCategoryState
    data object Error : DeleteCategoryState
    data object Deleted : DeleteCategoryState
}
