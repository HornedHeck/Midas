package com.hornedheck.midas.ui.transaction.list

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.category.Category
import com.hornedheck.midas.domain.model.transaction.Transaction
import com.hornedheck.midas.domain.model.transaction.TransactionFilter
import com.hornedheck.midas.domain.model.transaction.TransactionType
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.ISettingsRepo
import com.hornedheck.midas.domain.usecase.TransactionsListUseCase
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import com.hornedheck.midas.util.formatAmount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam

class TransactionListViewModel(
    @InjectedParam initialFilter: TransactionFilter? = null,
    private val useCase: TransactionsListUseCase,
    categoriesRepo: ICategoriesRepo,
    settingsRepo: ISettingsRepo,
) : ViewModel() {
    private val isSearchRequested = MutableStateFlow(false)
    val searchState = TextFieldState()

    init {
        if (initialFilter != null) {
            useCase.clearSearch()
            useCase.updateFilters(initialFilter)
        }
        searchState.setTextAndPlaceCursorAtEnd(useCase.searchQuery.value)
        viewModelScope.launch {
            snapshotFlow { searchState.text.toString() }
                .drop(1)
                .collectLatest { query ->
                    isSearchRequested.value = isSearchRequested.value || query.isNotBlank()
                    useCase.updateSearchQuery(query)
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TransactionListState> = settingsRepo.observeCurrency()
        .flatMapLatest { currency ->
            val currencyCode = currency.code
            combine(
                useCase.getTransactions()
                    .map { Result.success(it) }
                    .catch { e -> emit(Result.failure(e)) },
                useCase.filters,
                categoriesRepo.getCategories(),
                useCase.searchQuery,
                isSearchRequested,
            ) { transactionsResult, filter, categories, searchQuery, isSearchRequested ->
                val search = TransactionListSearchUi(
                    isVisible = isSearchRequested || searchQuery.isNotBlank(),
                    query = searchQuery,
                )

                transactionsResult.fold(
                    onSuccess = { transactions ->
                        transactions.toUiState(filter, categories, search, currencyCode)
                    },
                    onFailure = { error ->
                        TransactionListState.Error(
                            message = error.message ?: "",
                            activeChips = filter?.toChips(categories).orEmpty(),
                            search = search,
                            currencyCode = currencyCode,
                        )
                    },
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
            TransactionListState.Loading(),
        )

    fun showSearch() {
        isSearchRequested.value = true
    }

    fun clearSearchAndHide() {
        isSearchRequested.value = false
        searchState.clearText()
        useCase.clearSearch()
    }

    fun dismissChip(key: FilterChipKey) {
        val current = useCase.filters.value ?: return
        val updated = when (key) {
            is FilterChipKey.Type -> current.copy(type = TransactionType.ALL)
            is FilterChipKey.DateFrom -> current.copy(dateFrom = null)
            is FilterChipKey.DateTo -> current.copy(dateTo = null)
            is FilterChipKey.AmountFrom -> current.copy(amountFromCents = null)
            is FilterChipKey.AmountTo -> current.copy(amountToCents = null)
            is FilterChipKey.Category -> current.copy(categoryIds = current.categoryIds - key.id)
        }
        useCase.updateFilters(updated.takeUnless(TransactionFilter::isEmpty))
    }

    private fun List<Transaction>.toUiState(
        filter: TransactionFilter?,
        categories: List<Category>,
        search: TransactionListSearchUi,
        currencyCode: String,
    ): TransactionListState {
        val activeChips = filter?.toChips(categories).orEmpty()
        val groups = groupBy { it.date }
            .map { (date, items) ->
                TransactionGroup(
                    date = date,
                    transactions = items.map { it.toUiItem(currencyCode) },
                )
            }

        return if (groups.isEmpty()) {
            TransactionListState.Empty(
                reason = resolveEmptyReason(
                    hasActiveFilters = activeChips.isNotEmpty(),
                    hasSearchQuery = search.query.isNotBlank(),
                ),
                activeChips = activeChips,
                search = search,
                currencyCode = currencyCode,
            )
        } else {
            TransactionListState.Content(
                groups = groups,
                activeChips = activeChips,
                search = search,
                currencyCode = currencyCode,
            )
        }
    }

    private fun resolveEmptyReason(
        hasActiveFilters: Boolean,
        hasSearchQuery: Boolean,
    ): TransactionListEmptyReason = when {
        hasSearchQuery && hasActiveFilters -> TransactionListEmptyReason.SearchAndFilters
        hasSearchQuery -> TransactionListEmptyReason.Search
        hasActiveFilters -> TransactionListEmptyReason.Filters
        else -> TransactionListEmptyReason.None
    }

    private fun Transaction.toUiItem(currencyCode: String): TransactionUiItem {
        val isExpense = amountCents < 0
        return TransactionUiItem(
            id = id,
            description = description,
            categoryName = categoryName,
            formattedAmount = formatAmount(amountCents, currencyCode),
            isExpense = isExpense,
            categoryColor = categoryColor,
        )
    }

    private fun TransactionFilter.toChips(categories: List<Category>): List<FilterChipKey> {
        return buildList {
            type.toChipUi()?.let { add(FilterChipKey.Type(it)) }
            dateFrom?.let { add(FilterChipKey.DateFrom(it)) }
            dateTo?.let { add(FilterChipKey.DateTo(it)) }
            amountFromCents?.let { add(FilterChipKey.AmountFrom(it)) }
            amountToCents?.let { add(FilterChipKey.AmountTo(it)) }
            categoryIds.forEach { id ->
                val category = if (id == null) null else categories.find { it.id == id }
                add(
                    FilterChipKey.Category(
                        id = id,
                        name = category?.name,
                        color = category?.color,
                    )
                )
            }
        }
    }
}

private fun TransactionType.toChipUi(): TransactionTypeChipUi? = when (this) {
    TransactionType.ALL -> null
    TransactionType.EXPENSES -> TransactionTypeChipUi.Expense
    TransactionType.INCOME -> TransactionTypeChipUi.Income
}
