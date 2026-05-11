package com.hornedheck.midas.ui.transaction.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.settings.Currency
import com.hornedheck.midas.domain.repository.ISettingsRepo
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam

class TransactionDetailViewModel(
    @InjectedParam private val transactionId: Long,
    private val transactionsRepo: ITransactionsRepo,
    private val settingsRepo: ISettingsRepo,
) : ViewModel() {

    private val _transactionState = MutableStateFlow<TransactionDetailState>(TransactionDetailState.Loading)

    val state = combine(
        _transactionState,
        settingsRepo.observeCurrency(),
    ) { s, currency ->
        if (s is TransactionDetailState.Content) s.copy(currencyCode = currency.code) else s
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        TransactionDetailState.Loading,
    )

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _transactionState.value = TransactionDetailState.Loading
            runCatching { transactionsRepo.getTransactionById(transactionId) }
                .onSuccess { details ->
                    if (details == null) {
                        _transactionState.value = TransactionDetailState.Error
                    } else {
                        _transactionState.value = TransactionDetailState.Content(
                            id = details.id,
                            amountCents = details.amountCents,
                            isExpense = details.amountCents < 0,
                            description = details.description,
                            date = details.date,
                            categoryName = details.categoryName,
                            notes = details.notes,
                            currencyCode = Currency.EUR.code,
                        )
                    }
                }
                .onFailure { _transactionState.value = TransactionDetailState.Error }
        }
    }
}
