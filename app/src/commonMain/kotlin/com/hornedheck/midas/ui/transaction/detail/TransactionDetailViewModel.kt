package com.hornedheck.midas.ui.transaction.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val transactionId: Long,
    private val transactionsRepo: ITransactionsRepo,
) : ViewModel() {

    private val _state = MutableStateFlow<TransactionDetailState>(TransactionDetailState.Loading)
    val state: StateFlow<TransactionDetailState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = TransactionDetailState.Loading
            runCatching { transactionsRepo.getTransactionById(transactionId) }
                .onSuccess { details ->
                    if (details == null) {
                        _state.value = TransactionDetailState.Error("")
                    } else {
                        _state.value = TransactionDetailState.Content(
                            id = details.id,
                            amountCents = details.amountCents,
                            isExpense = details.amountCents < 0,
                            description = details.description,
                            date = details.datetime.date,
                            categoryName = details.categoryName,
                            notes = details.notes,
                        )
                    }
                }
                .onFailure { _state.value = TransactionDetailState.Error("") }
        }
    }
}
