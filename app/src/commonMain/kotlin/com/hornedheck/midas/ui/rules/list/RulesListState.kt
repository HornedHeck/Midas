package com.hornedheck.midas.ui.rules.list

data class RuleUiItem(
    val id: Long,
    val label: String,
    val categoryName: String?,
    val categoryColor: Int?,
)

sealed interface RulesListState {
    data object Loading : RulesListState
    data object Empty : RulesListState
    data class Error(val message: String = "") : RulesListState
    data class Content(
        val items: List<RuleUiItem>,
        val reapplyStatus: ReapplyStatus = ReapplyStatus.Idle,
    ) : RulesListState
}

sealed interface ReapplyStatus {
    data object Idle : ReapplyStatus
    data object Loading : ReapplyStatus
    data class Success(val count: Int) : ReapplyStatus
    data object Error : ReapplyStatus
}
