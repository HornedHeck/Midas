package com.hornedheck.midas.domain.usecase

import com.hornedheck.midas.domain.model.TransactionFilter
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class TransactionsListUseCase(private val repo: ITransactionsRepo) {

    private val _filters = MutableStateFlow<TransactionFilter?>(null)
    val filters = _filters.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTransactions() = _filters.flatMapLatest(repo::getTransactions)

    fun updateFilters(filters: TransactionFilter?) {
        this._filters.value = filters
    }

}
