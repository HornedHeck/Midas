package com.hornedheck.midas.ui.transaction.list

import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

import midas.app.generated.resources.Res
import midas.app.generated.resources.empty_transactions
import midas.app.generated.resources.empty_transactions_filtered
import midas.app.generated.resources.empty_transactions_search
import midas.app.generated.resources.empty_transactions_search_filtered
import midas.app.generated.resources.filter_type_expense
import midas.app.generated.resources.filter_type_income

sealed interface TransactionListState {
    val search: TransactionListSearchUi

    data class Loading(
        override val search: TransactionListSearchUi = TransactionListSearchUi(),
    ) : TransactionListState

    data class Empty(
        val reason: TransactionListEmptyReason = TransactionListEmptyReason.None,
        val activeChips: List<FilterChipKey> = emptyList(),
        override val search: TransactionListSearchUi = TransactionListSearchUi(),
    ) : TransactionListState

    data class Content(
        val groups: List<TransactionGroup>,
        val activeChips: List<FilterChipKey>,
        override val search: TransactionListSearchUi = TransactionListSearchUi(),
    ) : TransactionListState

    data class Error(
        val message: String,
        val activeChips: List<FilterChipKey> = emptyList(),
        override val search: TransactionListSearchUi = TransactionListSearchUi(),
    ) : TransactionListState
}

data class TransactionListSearchUi(
    val isVisible: Boolean = false,
    val query: String = "",
)

enum class TransactionListEmptyReason(val message: StringResource) {
    None(Res.string.empty_transactions),
    Filters(Res.string.empty_transactions_filtered),
    Search(Res.string.empty_transactions_search),
    SearchAndFilters(Res.string.empty_transactions_search_filtered),
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
