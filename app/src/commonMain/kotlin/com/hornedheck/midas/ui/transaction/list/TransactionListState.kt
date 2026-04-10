package com.hornedheck.midas.ui.transaction.list

import kotlinx.datetime.LocalDate

sealed interface TransactionListState {
    data object Loading : TransactionListState
    data object Empty : TransactionListState
    data class Content(val groups: List<TransactionGroup>) : TransactionListState
    data class Error(val message: String) : TransactionListState
}

data class TransactionGroup(
    val date: LocalDate,
    val transactions: List<TransactionUiItem>,
)

data class TransactionUiItem(
    val id: String,
    val description: String,
    val categoryName: String?,
    val formattedAmount: String,
    val isExpense: Boolean,
    val categoryColor: Int?,
)
