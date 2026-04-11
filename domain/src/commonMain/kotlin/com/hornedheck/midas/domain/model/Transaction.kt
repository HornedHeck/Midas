package com.hornedheck.midas.domain.model

import kotlinx.datetime.LocalDateTime

data class Transaction(
    val id: String,
    val datetime: LocalDateTime,
    val amountCents: Long,
    val description: String,
    val categoryName: String?,
    val categoryColor: Int?,
)
