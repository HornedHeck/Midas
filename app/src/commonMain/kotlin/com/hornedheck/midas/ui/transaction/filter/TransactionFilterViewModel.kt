package com.hornedheck.midas.ui.transaction.filter

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.TransactionFilter
import com.hornedheck.midas.domain.model.TransactionType
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.usecase.TransactionsListUseCase
import com.hornedheck.midas.util.formatAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private const val CentsPerWhole = 100L
private const val MaxAmountParts = 2
private const val MaxFractionDigits = 2

class TransactionFilterViewModel(
    private val useCase: TransactionsListUseCase,
    categoriesRepo: ICategoriesRepo,
) : ViewModel() {

    val amountFromState = TextFieldState()
    val amountToState = TextFieldState()

    private val _state = MutableStateFlow(buildInitialFormState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            categoriesRepo.getCategories().collect { categories ->
                _state.update {
                    it.copy(categories = categories)
                }
            }
        }
    }

    private fun buildInitialFormState(): TransactionFilterFormState {
        val current = useCase.filters.value
        val dateFrom = current?.dateFrom
        val dateTo = current?.dateTo
        current?.amountFromCents?.let {
            amountFromState.setTextAndPlaceCursorAtEnd(formatAmount(it))
        }
        current?.amountToCents?.let {
            amountToState.setTextAndPlaceCursorAtEnd(formatAmount(it))
        }
        return TransactionFilterFormState(
            type = current?.type ?: TransactionType.ALL,
            dateFrom = dateFrom,
            dateTo = dateTo,
            selectedQuickRange = resolveQuickRange(dateFrom = dateFrom, dateTo = dateTo),
            selectedCategoryIds = current?.categoryIds ?: emptySet(),
        )
    }

    fun updateType(type: TransactionType) {
        _state.update { it.copy(type = type) }
    }

    fun updateDateFrom(date: LocalDate?) {
        _state.update {
            it.copy(
                dateFrom = date,
                selectedQuickRange = resolveQuickRange(dateFrom = date, dateTo = it.dateTo),
            )
        }
    }

    fun updateDateTo(date: LocalDate?) {
        _state.update {
            it.copy(
                dateTo = date,
                selectedQuickRange = resolveQuickRange(dateFrom = it.dateFrom, dateTo = date),
            )
        }
    }

    fun applyQuickRange(range: QuickDateRange) {
        val (from, to) = range.dateRange(
            today = Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
        _state.update {
            it.copy(
                dateFrom = from,
                dateTo = to,
                selectedQuickRange = range,
            )
        }
    }

    fun updateSelectedCategories(ids: Set<Long?>) {
        _state.update { it.copy(selectedCategoryIds = ids) }
    }

    fun clearAll() {
        amountFromState.clearText()
        amountToState.clearText()
        useCase.updateFilters(null)
        _state.update {
            it.copy(
                type = TransactionType.ALL,
                dateFrom = null,
                dateTo = null,
                selectedQuickRange = null,
                selectedCategoryIds = emptySet(),
                amountError = false,
                applied = true,
            )
        }
    }

    fun apply() {
        val amountFrom = amountFromState.text.toString().toAmountCents()
        val amountTo = amountToState.text.toString().toAmountCents()

        if (amountFrom != null && amountTo != null && amountFrom > amountTo) {
            _state.update { it.copy(amountError = true) }
            return
        }

        val current = _state.value
        _state.update { it.copy(amountError = false) }
        useCase.updateFilters(
            TransactionFilter(
                type = current.type,
                dateFrom = current.dateFrom,
                dateTo = current.dateTo,
                amountFromCents = amountFrom,
                amountToCents = amountTo,
                categoryIds = current.selectedCategoryIds,
            )
        )
        _state.update { it.copy(applied = true) }
    }

    fun clearApplied() {
        _state.update { it.copy(applied = false) }
    }
}

@Suppress("MagicNumber", "ReturnCount")
private fun String.toAmountCents(): Long? {
    if (isEmpty()) return null

    val parts = split('.')
    if (parts.size > 2) return null

    var amount = parts.first().toLong() * 100
    if (parts.size == 2) {
        amount += parts[1].padEnd(2, '0').toLong()
    }
    return amount
}

private fun resolveQuickRange(dateFrom: LocalDate?, dateTo: LocalDate?): QuickDateRange? {
    if (dateFrom == null || dateTo == null) return null

    return QuickDateRange.entries.firstOrNull { range ->
        val (rangeFrom, rangeTo) = range.dateRange(today = dateTo)
        dateFrom == rangeFrom && dateTo == rangeTo
    }
}

private fun QuickDateRange.dateRange(today: LocalDate): Pair<LocalDate, LocalDate> {
    return when (this) {
        QuickDateRange.THIS_WEEK -> {
            val weekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
            weekStart to today
        }
        QuickDateRange.PAST_WEEK -> {
            val lastWeekEnd = today.minus(today.dayOfWeek.ordinal + 1, DateTimeUnit.DAY)
            val lastWeekStart = lastWeekEnd.minus(DayOfWeek.entries.size - 1, DateTimeUnit.DAY)
            lastWeekStart to lastWeekEnd
        }
        QuickDateRange.THIS_MONTH -> {
            val monthStart = LocalDate(today.year, today.month, 1)
            monthStart to today
        }
        QuickDateRange.PAST_MONTH -> {
            val firstOfCurrentMonth = LocalDate(today.year, today.month, 1)
            val lastMonthEnd = firstOfCurrentMonth.minus(1, DateTimeUnit.DAY)
            val lastMonthStart = LocalDate(lastMonthEnd.year, lastMonthEnd.month, 1)
            lastMonthStart to lastMonthEnd
        }
    }
}
