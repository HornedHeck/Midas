@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.home

import com.hornedheck.midas.domain.model.dashboard.CategorySpendingSummary
import com.hornedheck.midas.domain.model.dashboard.TrendDelta

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Empty(val selectedRange: HomeRange) : HomeUiState

    data class Error(val selectedRange: HomeRange) : HomeUiState

    data class Content(
        val selectedRange: HomeRange,
        val incomeCents: Long,
        val expensesCents: Long,
        val netBalanceCents: Long,
        val incomeDelta: TrendDelta?,
        val expensesDelta: TrendDelta?,
        val netBalanceDelta: TrendDelta?,
        val categories: List<CategorySpendingSummary>,
    ) : HomeUiState
}
