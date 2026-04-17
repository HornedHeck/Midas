package com.hornedheck.midas.ui.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import com.hornedheck.midas.util.formatAmount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TransactionListViewModel(
    repo: ITransactionsRepo,
) : ViewModel() {

    val state: StateFlow<TransactionListState> = repo.getTransactions()
        .map { transactions -> transactions.toUiState() }
        .catch { e -> emit(TransactionListState.Error(e.message ?: "")) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
            TransactionListState.Loading,
        )

    private fun List<Transaction>.toUiState(): TransactionListState {
        val groups = groupBy { it.datetime.date }
            .map { (date, items) ->
                TransactionGroup(
                    date = date,
                    transactions = items.map { it.toUiItem() },
                )
            }
        return if (groups.isEmpty()) TransactionListState.Empty
        else TransactionListState.Content(groups)
    }

    private fun Transaction.toUiItem(): TransactionUiItem {
        val isExpense = amountCents < 0
        return TransactionUiItem(
            id = id,
            description = description,
            categoryName = categoryName,
            formattedAmount = formatAmount(amountCents),
            isExpense = isExpense,
            categoryColor = categoryColor,
        )
    }
}
