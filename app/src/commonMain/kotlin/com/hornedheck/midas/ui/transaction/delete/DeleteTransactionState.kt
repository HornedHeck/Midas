package com.hornedheck.midas.ui.transaction.delete

sealed interface DeleteTransactionState {
    data object Idle : DeleteTransactionState
    data object Loading : DeleteTransactionState
    data object Error : DeleteTransactionState
    data object Success : DeleteTransactionState
}
