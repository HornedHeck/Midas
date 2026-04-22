package com.hornedheck.midas.ui.transaction.list

import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

import midas.app.generated.resources.Res
import midas.app.generated.resources.filter_type_expense
import midas.app.generated.resources.filter_type_income

sealed interface TransactionListState {
    data object Loading : TransactionListState
    data class Empty(
        val isFiltered: Boolean = false,
        val activeChips: List<FilterChipKey> = emptyList(),
    ) : TransactionListState
    data class Content(
        val groups: List<TransactionGroup>,
        val activeChips: List<FilterChipKey>,
    ) : TransactionListState
    data class Error(
        val message: String,
        val activeChips: List<FilterChipKey> = emptyList(),
    ) : TransactionListState
}

sealed interface FilterChipKey {
    data class Type(val type: TransactionTypeChipUi) : FilterChipKey
    data class DateFrom(val date: LocalDate) : FilterChipKey
    data class DateTo(val date: LocalDate) : FilterChipKey
    data class AmountFrom(val cents: Long) : FilterChipKey
    data class AmountTo(val cents: Long) : FilterChipKey
    data class Category(
        val id: Long?,
        val name: String?,
        val color: Int?,
    ) : FilterChipKey
}

enum class TransactionTypeChipUi(val label: StringResource) {
    Expense(Res.string.filter_type_expense),
    Income(Res.string.filter_type_income),
}

data class TransactionGroup(
    val date: LocalDate,
    val transactions: List<TransactionUiItem>,
)

data class TransactionUiItem(
    val id: Long,
    val description: String,
    val categoryName: String?,
    val formattedAmount: String,
    val isExpense: Boolean,
    val categoryColor: Int?,
)
