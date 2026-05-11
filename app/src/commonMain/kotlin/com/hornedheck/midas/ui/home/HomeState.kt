@file:Suppress("MatchingDeclarationName")

package com.hornedheck.midas.ui.home

import com.hornedheck.midas.domain.model.dashboard.CategorySpendingSummary

sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Empty(val selectedRange: HomeRange) : HomeUiState

    data class Error(val selectedRange: HomeRange) : HomeUiState

    data class Content(
        val selectedRange: HomeRange,
        val incomeCents: Long,
        val expensesCents: Long,
        val netBalanceCents: Long,
        val incomeDeltaPct: Float?,
        val isIncomeTrendPositive: Boolean?,
        val expensesDeltaPct: Float?,
        val isExpensesTrendPositive: Boolean?,
        val netBalanceDeltaPct: Float?,
        val isNetBalanceTrendPositive: Boolean?,
        val categories: List<CategorySpendingSummary>,
    ) : HomeUiState
}
