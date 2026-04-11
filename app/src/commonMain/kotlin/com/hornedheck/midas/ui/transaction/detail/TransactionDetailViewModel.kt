package com.hornedheck.midas.ui.transaction.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val transactionsRepo: ITransactionsRepo,
) : ViewModel() {

    private var transactionId: Long? = null

    private val _state = MutableStateFlow<TransactionDetailState>(TransactionDetailState.Loading)
    val state: StateFlow<TransactionDetailState> = _state.asStateFlow()

    fun init(id: Long) {
        if (transactionId != id) {
            transactionId = id
            load()
        }
    }

    fun load() {
        val id = transactionId ?: return
        viewModelScope.launch {
            _state.value = TransactionDetailState.Loading
            runCatching { transactionsRepo.getTransactionById(id) }
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
