package com.hornedheck.midas.ui.transaction.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeleteTransactionViewModel(
    private val transactionId: Long,
    private val transactionsRepo: ITransactionsRepo,
) : ViewModel() {

    private val _state = MutableStateFlow<DeleteTransactionState>(DeleteTransactionState.Idle)
    val state: StateFlow<DeleteTransactionState> = _state

    fun confirmDelete() {
        viewModelScope.launch {
            _state.value = DeleteTransactionState.Loading
            runCatching { transactionsRepo.deleteTransaction(transactionId) }
                .onSuccess { _state.value = DeleteTransactionState.Success }
                .onFailure { _state.value = DeleteTransactionState.Error }
        }
    }

    fun clearSuccess() {
        if (_state.value is DeleteTransactionState.Success) {
            _state.value = DeleteTransactionState.Idle
        }
    }
}
