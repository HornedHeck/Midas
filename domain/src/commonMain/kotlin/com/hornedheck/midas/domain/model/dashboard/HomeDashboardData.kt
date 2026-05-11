package com.hornedheck.midas.domain.model.dashboard

data class HomeDashboardData(
    val currentIncomeCents: Long,
    val currentExpensesCents: Long,
    val previousIncomeCents: Long,
    val previousExpensesCents: Long,
    val breakdown: List<CategorySpending>,
)
