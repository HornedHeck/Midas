package com.hornedheck.midas.ui.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import com.hornedheck.midas.formatAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class TransactionListViewModel(
    private val repo: ITransactionsRepo,
) : ViewModel() {

    private val _state = MutableStateFlow<TransactionListState>(TransactionListState.Loading)
    val state: StateFlow<TransactionListState> = _state.asStateFlow()

    init {
        repo.getTransactions()
            .map { transactions -> transactions.toUiState() }
            .catch { e -> emit(TransactionListState.Error(e.message ?: "")) }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

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
