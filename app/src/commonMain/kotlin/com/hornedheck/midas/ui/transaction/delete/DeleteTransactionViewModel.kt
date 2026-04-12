package com.hornedheck.midas.ui.transaction.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DeleteTransactionViewModel(
    private val transactionId: Long,
    private val transactionsRepo: ITransactionsRepo,
) : ViewModel() {

    private val _state = MutableStateFlow<DeleteTransactionState>(DeleteTransactionState.Idle)
    val state: StateFlow<DeleteTransactionState> = _state

    private val _deletedEvent = MutableSharedFlow<Unit>()
    val deletedEvent: SharedFlow<Unit> = _deletedEvent

    fun confirmDelete() {
        viewModelScope.launch {
            _state.value = DeleteTransactionState.Loading
            runCatching { transactionsRepo.deleteTransaction(transactionId) }
                .onSuccess { _deletedEvent.emit(Unit) }
                .onFailure { _state.value = DeleteTransactionState.Error }
        }
    }
}
