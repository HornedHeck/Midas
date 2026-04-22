package com.hornedheck.midas.domain.model

import kotlinx.datetime.LocalDate

data class TransactionFilter(
    val type: TransactionType = TransactionType.ALL,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val amountFromCents: Long? = null,
    val amountToCents: Long? = null,
    val categoryIds: Set<Long?> = emptySet(),
) {

    val isEmpty: Boolean
        get() = type == TransactionType.ALL
                && dateFrom == null
                && dateTo == null
                && amountFromCents == null
                && amountToCents == null
                && categoryIds.isEmpty()
}

enum class TransactionType { ALL, EXPENSES, INCOME }
