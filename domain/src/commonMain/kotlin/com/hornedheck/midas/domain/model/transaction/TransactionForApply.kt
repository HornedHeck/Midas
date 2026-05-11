package com.hornedheck.midas.domain.model.transaction

import kotlinx.datetime.LocalDate

data class TransactionForApply(
    val id: Long,
    val date: LocalDate,
    val amountCents: Long,
    val description: String,
    val categoryId: Long?,
    val categorySource: CategorySource,
)
