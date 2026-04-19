package com.hornedheck.midas.ui.rules.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.IRulesRepo
import com.hornedheck.midas.domain.usecase.ApplyRulesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RulesListViewModel(
    private val rulesRepo: IRulesRepo,
    categoriesRepo: ICategoriesRepo,
    private val applyRulesUseCase: ApplyRulesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<RulesListState>(RulesListState.Loading)
    val state: StateFlow<RulesListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(rulesRepo.getRules(), categoriesRepo.getCategories()) { rules, categories ->
                val categoryMap = categories.associateBy { it.id }
                rules.map { rule ->
                    RuleUiItem(
                        id = rule.id,
                        label = rule.value,
                        categoryName = categoryMap[rule.categoryId]?.name,
                        categoryColor = categoryMap[rule.categoryId]?.color,
                    )
                }
            }.collect { uiItems ->
                if (uiItems.isEmpty()) {
                    _state.value = RulesListState.Empty
                } else {
                    val current = _state.value
                    val reapplyStatus = (current as? RulesListState.Content)?.reapplyStatus
                        ?: ReapplyStatus.Idle
                    _state.value = RulesListState.Content(uiItems, reapplyStatus)
                }
            }
        }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch {
            runCatching { rulesRepo.deleteRule(id) }
        }
    }

    fun moveItem(from: Int, to: Int) {
        val current = _state.value as? RulesListState.Content ?: return
        val items = current.items.toMutableList()
        if (from !in items.indices || to !in items.indices) return
        val item = items.removeAt(from)
        items.add(to, item)
        _state.update { s ->
            (s as? RulesListState.Content)?.copy(items = items) ?: s
        }
        viewModelScope.launch {
            runCatching { rulesRepo.reorderRules(items.map { it.id }) }
        }
    }

    fun reapply() {
        val current = _state.value as? RulesListState.Content ?: return
        _state.value = current.copy(reapplyStatus = ReapplyStatus.Loading)
        viewModelScope.launch {
            runCatching { applyRulesUseCase() }
                .onSuccess { count ->
                    _state.update { s ->
                        (s as? RulesListState.Content)?.copy(
                            reapplyStatus = ReapplyStatus.Success(count)
                        ) ?: s
                    }
                }
                .onFailure {
                    _state.update { s ->
                        (s as? RulesListState.Content)?.copy(reapplyStatus = ReapplyStatus.Error) ?: s
                    }
                }
        }
    }

    fun clearReapplyStatus() {
        _state.update { s ->
            (s as? RulesListState.Content)?.copy(reapplyStatus = ReapplyStatus.Idle) ?: s
        }
    }
}
