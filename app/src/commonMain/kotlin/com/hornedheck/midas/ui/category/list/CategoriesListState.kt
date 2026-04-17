package com.hornedheck.midas.ui.category.list

sealed interface CategoriesListState {
    data object Loading : CategoriesListState
    data object Empty : CategoriesListState
    data class Content(val items: List<CategoryUiItem>) : CategoriesListState
    data class Error(val message: String) : CategoriesListState
}

data class CategoryUiItem(
    val id: Long,
    val name: String,
    val color: Int,
)
