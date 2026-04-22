package com.hornedheck.midas.domain.model

import kotlinx.datetime.LocalDate

data class TransactionDetails(
    val id: Long,
    val date: LocalDate,
    val amountCents: Long,
    val description: String,
    val categoryId: Long?,
    val categoryName: String?,
    val notes: String?,
    val categoryColor: Int?,
    val categorySource: CategorySource = CategorySource.MANUAL,
)
