package com.hornedheck.midas.domain.model.dashboard

data class CategorySpendingSummary(
    val categoryId: Long?,
    val name: String?,
    val color: Int?,
    val totalCents: Long,
    val percentage: Float,
    val isOthers: Boolean = false,
)
