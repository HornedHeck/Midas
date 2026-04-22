package com.hornedheck.midas.ui.transaction.filter

import com.hornedheck.midas.domain.model.Category
import com.hornedheck.midas.domain.model.TransactionType
import kotlinx.datetime.LocalDate

data class TransactionFilterFormState(
    val type: TransactionType = TransactionType.ALL,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val selectedQuickRange: QuickDateRange? = null,
    val categories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<Long?> = emptySet(),
    val amountError: Boolean = false,
    val applied: Boolean = false,
)
