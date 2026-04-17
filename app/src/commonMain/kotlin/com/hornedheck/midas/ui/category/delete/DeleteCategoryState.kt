package com.hornedheck.midas.ui.category.delete

sealed interface DeleteCategoryState {
    data object Idle : DeleteCategoryState
    data object Loading : DeleteCategoryState
    data object Error : DeleteCategoryState
    data object Success : DeleteCategoryState
}
