package com.hornedheck.midas.ui.category.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeleteCategoryViewModel(
    private val categoryId: Long,
    private val categoriesRepo: ICategoriesRepo,
) : ViewModel() {

    private val _state = MutableStateFlow<DeleteCategoryState>(DeleteCategoryState.Idle)
    val state: StateFlow<DeleteCategoryState> = _state.asStateFlow()

    fun confirmDelete() {
        viewModelScope.launch {
            _state.value = DeleteCategoryState.Loading
            runCatching { categoriesRepo.deleteCategory(categoryId) }
                .onSuccess { _state.value = DeleteCategoryState.Success }
                .onFailure { _state.value = DeleteCategoryState.Error }
        }
    }

    fun clearSuccess() {
        if (_state.value is DeleteCategoryState.Success) {
            _state.value = DeleteCategoryState.Idle
        }
    }
}
