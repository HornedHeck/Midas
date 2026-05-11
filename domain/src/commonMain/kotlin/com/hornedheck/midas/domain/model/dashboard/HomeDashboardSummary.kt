package com.hornedheck.midas.domain.model.dashboard

data class HomeDashboardSummary(
    val incomeCents: Long,
    val expensesCents: Long,
    val netBalanceCents: Long,
    val incomeDeltaPct: Float?,
    val expensesDeltaPct: Float?,
    val netBalanceDeltaPct: Float?,
    val categories: List<CategorySpendingSummary>,
    val isEmpty: Boolean,
)
