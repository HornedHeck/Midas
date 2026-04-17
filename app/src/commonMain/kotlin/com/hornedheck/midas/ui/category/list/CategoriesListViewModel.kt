package com.hornedheck.midas.ui.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CategoriesListViewModel(
    repo: ICategoriesRepo,
) : ViewModel() {

    val state: StateFlow<CategoriesListState> = repo.getCategories()
        .map { categories ->
            if (categories.isEmpty()) CategoriesListState.Empty
            else CategoriesListState.Content(
                categories.map { CategoryUiItem(it.id, it.name, it.color) }
            )
        }
        .catch { e -> emit(CategoriesListState.Error(e.message ?: "")) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
            CategoriesListState.Loading,
        )
}
