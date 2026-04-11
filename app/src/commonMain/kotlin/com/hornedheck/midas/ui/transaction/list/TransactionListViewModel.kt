package com.hornedheck.midas.ui.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import com.hornedheck.midas.formatAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TransactionListViewModel(
    private val repo: ITransactionsRepo,
) : ViewModel() {

    private val _state = MutableStateFlow<TransactionListState>(TransactionListState.Loading)
    val state: StateFlow<TransactionListState> = _state.asStateFlow()

    init {
        loadTransactions()
        repo.changes()
            .onEach { loadTransactions() }
            .launchIn(viewModelScope)
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _state.value = TransactionListState.Loading
            runCatching { repo.getTransactions() }
                .onSuccess { transactions ->
                    val groups = transactions
                        .groupBy { it.datetime.date }
                        .map { (date, items) ->
                            TransactionGroup(
                                date = date,
                                transactions = items.map { it.toUiItem() },
                            )
                        }
                    _state.value = if (groups.isEmpty()) {
                        TransactionListState.Empty
                    } else {
                        TransactionListState.Content(groups)
                    }
                }
                .onFailure { e ->
                    _state.value = TransactionListState.Error(e.message ?: "")
                }
        }
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
