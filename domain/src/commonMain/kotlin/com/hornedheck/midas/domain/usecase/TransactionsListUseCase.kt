package com.hornedheck.midas.domain.usecase

import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.model.TransactionFilter
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

private const val SearchDebounceMillis = 500L

@OptIn(FlowPreview::class)
class TransactionsListUseCase(private val repo: ITransactionsRepo) {

    // Filters owned by the filter screen. Search stays separate and is merged later.
    private val _baseFilters = MutableStateFlow<TransactionFilter?>(null)
    val filters = _baseFilters.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val appliedFilters = combine(
        _baseFilters,
        _searchQuery
            .debounce { query ->
                if (query.isBlank()) {
                    0L
                } else {
                    SearchDebounceMillis
                }
            }
            .map(::normalizeSearchQuery)
            .distinctUntilChanged(),
    ) { filters, searchQuery ->
        mergeFilters(filters, searchQuery)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun getTransactions(): Flow<List<Transaction>> =
        appliedFilters.flatMapLatest(repo::getTransactions)

    fun updateFilters(filters: TransactionFilter?) {
        _baseFilters.value = filters
            ?.withoutSearch()
            ?.takeUnless(TransactionFilter::isEmpty)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    private fun mergeFilters(filters: TransactionFilter?, searchQuery: String?): TransactionFilter? {
        val merged = (filters ?: TransactionFilter()).withSearchQuery(searchQuery)
        return merged.takeUnless(TransactionFilter::isEmpty)
    }

    private fun normalizeSearchQuery(query: String): String? =
        query.trim().takeIf { it.isNotEmpty() }
}

private fun TransactionFilter.withoutSearch(): TransactionFilter = copy(searchQuery = null)
