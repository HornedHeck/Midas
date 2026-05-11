package com.hornedheck.midas.domain.model.transaction

import kotlinx.datetime.LocalDate

data class Transaction(
    val id: Long,
    val date: LocalDate,
    val amountCents: Long,
    val description: String,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryColor: Int?,
)
