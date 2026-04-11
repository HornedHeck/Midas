package com.hornedheck.midas.domain.model

import kotlinx.datetime.LocalDateTime

data class TransactionDetails(
    val id: Long,
    val datetime: LocalDateTime,
    val amountCents: Long,
    val description: String,
    val categoryId: String?,
    val categoryName: String?,
    val notes: String?,
    val categoryColor: Int?,
)
