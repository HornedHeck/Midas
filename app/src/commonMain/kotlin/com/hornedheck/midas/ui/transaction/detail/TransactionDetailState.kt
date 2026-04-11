package com.hornedheck.midas.ui.transaction.detail

import kotlinx.datetime.LocalDate

sealed interface TransactionDetailState {
    data object Loading : TransactionDetailState
    data class Error(val message: String) : TransactionDetailState
    data class Content(
        val id: Long,
        val amountCents: Long,
        val isExpense: Boolean,
        val description: String,
        val date: LocalDate,
        val categoryName: String?,
        val notes: String?,
    ) : TransactionDetailState
}
