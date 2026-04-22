package com.hornedheck.midas.domain.model

import kotlinx.datetime.LocalDate

data class TransactionFilter(
    val type: TransactionType = TransactionType.ALL,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val amountFromCents: Long? = null,
    val amountToCents: Long? = null,
    val categoryIds: Set<Long?> = emptySet(),
    val searchQuery: String? = null,
) {
    fun withSearchQuery(query: String?): TransactionFilter =
        copy(searchQuery = query?.trim()?.takeIf { it.isNotEmpty() })

    val isEmpty: Boolean
        get() = type == TransactionType.ALL
                && dateFrom == null
                && dateTo == null
                && amountFromCents == null
                && amountToCents == null
                && categoryIds.isEmpty()
                && searchQuery.isNullOrBlank()
}

enum class TransactionType { ALL, EXPENSES, INCOME }
