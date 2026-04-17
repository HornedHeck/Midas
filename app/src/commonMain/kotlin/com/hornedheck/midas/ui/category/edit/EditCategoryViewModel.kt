package com.hornedheck.midas.ui.category.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import midas.app.generated.resources.Res
import midas.app.generated.resources.error_name_required
import midas.app.generated.resources.error_save_category_failed

class EditCategoryViewModel(
    private val id: Long?,
    private val repo: ICategoriesRepo,
) : ViewModel() {

    private val _state = MutableStateFlow(EditCategoryState())
    val state: StateFlow<EditCategoryState> = _state.asStateFlow()

    init {
        if (id != null) {
            viewModelScope.launch {
                runCatching { repo.getCategoryById(id) }
                    .onSuccess { category ->
                        category?.let {
                            _state.value.form.nameState.edit { replace(0, length, it.name) }
                            _state.update { s -> s.copy(form = s.form.copy(selectedColor = category.color)) }
                        }
                    }
            }
        }
    }

    fun selectColor(color: Int) {
        _state.update { it.copy(form = it.form.copy(selectedColor = color)) }
    }

    fun clearSuccess() {
        if (_state.value.status is EditCategoryStatus.Success) {
            _state.update { it.copy(status = EditCategoryStatus.Idle) }
        }
    }

    fun clearError() {
        if (_state.value.status is EditCategoryStatus.Error) {
            _state.update { it.copy(status = EditCategoryStatus.Idle) }
        }
    }

    fun save() {
        val current = _state.value
        if (current.status is EditCategoryStatus.Loading || current.status is EditCategoryStatus.Success) return

        val name = current.form.nameState.text.toString().trim()
        if (name.isBlank()) {
            _state.update { it.copy(form = it.form.copy(nameError = Res.string.error_name_required)) }
            return
        }

        val color = current.form.selectedColor
        _state.update { it.copy(status = EditCategoryStatus.Loading) }

        viewModelScope.launch {
            runCatching {
                if (id == null) {
                    repo.addCategory(name, color)
                } else {
                    repo.updateCategory(id, name, color)
                }
            }.onSuccess {
                _state.update { it.copy(status = EditCategoryStatus.Success) }
            }.onFailure {
                _state.update { it.copy(status = EditCategoryStatus.Error(Res.string.error_save_category_failed)) }
            }
        }
    }
}
