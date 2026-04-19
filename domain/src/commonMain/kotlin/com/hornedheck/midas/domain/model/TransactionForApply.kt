package com.hornedheck.midas.domain.model

import kotlinx.datetime.LocalDateTime

data class TransactionForApply(
    val id: Long,
    val datetime: LocalDateTime,
    val amountCents: Long,
    val description: String,
    val categoryId: Long?,
    val categorySource: CategorySource,
)
