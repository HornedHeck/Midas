package com.hornedheck.midas.ui.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.Category
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.model.TransactionFilter
import com.hornedheck.midas.domain.model.TransactionType
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.usecase.TransactionsListUseCase
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import com.hornedheck.midas.util.formatAmount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

class TransactionListViewModel(
    private val useCase: TransactionsListUseCase,
    categoriesRepo: ICategoriesRepo,
    ) : ViewModel() {

        val state: StateFlow<TransactionListState> = combine(
            useCase.getTransactions()
                .map<List<Transaction>, Result<List<Transaction>>> { Result.success(it) }
                .catch { e -> emit(Result.failure(e)) },
            useCase.filters,
            categoriesRepo.getCategories(),
        ) { transactionsResult, filter, categories ->
            transactionsResult.fold(
                onSuccess = { transactions ->
                    transactions.toUiState(filter, categories)
                },
                onFailure = { error ->
                    TransactionListState.Error(
                        message = error.message ?: "",
                        activeChips = filter?.toChips(categories).orEmpty(),
                    )
                },
            )
        }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
                TransactionListState.Loading,
            )

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
        useCase.updateFilters(if (updated.isEmpty) null else updated)
    }

    private fun List<Transaction>.toUiState(
        filter: TransactionFilter?,
        categories: List<Category>,
    ): TransactionListState {
        val activeChips = filter?.toChips(categories).orEmpty()
        val groups = groupBy { it.date }
            .map { (date, items) ->
                TransactionGroup(
                    date = date,
                    transactions = items.map { it.toUiItem() },
                )
            }
        return if (groups.isEmpty()) {
            TransactionListState.Empty(
                isFiltered = activeChips.isNotEmpty(),
                activeChips = activeChips,
            )
        } else {
            TransactionListState.Content(
                groups = groups,
                activeChips = activeChips,
            )
        }
    }

    private fun Transaction.toUiItem(): TransactionUiItem {
        val isExpense = amountCents < 0
        return TransactionUiItem(
            id = id,
            description = description,
            categoryName = categoryName,
            formattedAmount = formatAmount(amountCents, withCurrency = true),
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
