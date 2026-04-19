package com.hornedheck.midas.ui.category.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.IRulesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam

class DeleteCategoryViewModel(
    @InjectedParam private val categoryId: Long,
    private val categoriesRepo: ICategoriesRepo,
    private val rulesRepo: IRulesRepo,
) : ViewModel() {

    private val _state = MutableStateFlow<DeleteCategoryState>(DeleteCategoryState.Loading)
    val state: StateFlow<DeleteCategoryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val count = rulesRepo.countRulesForCategory(categoryId)
            _state.value = DeleteCategoryState.Confirm(count)
        }
    }

    fun confirmDelete() {
        if (_state.value !is DeleteCategoryState.Confirm) return
        viewModelScope.launch {
            _state.value = DeleteCategoryState.Deleting
            runCatching { categoriesRepo.deleteCategory(categoryId) }
                .onSuccess { _state.value = DeleteCategoryState.Deleted }
                .onFailure { _state.value = DeleteCategoryState.Error }
        }
    }
}
