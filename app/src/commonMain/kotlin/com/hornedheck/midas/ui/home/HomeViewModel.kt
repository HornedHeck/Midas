package com.hornedheck.midas.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.settings.Currency
import com.hornedheck.midas.domain.model.settings.DashboardRange
import com.hornedheck.midas.domain.repository.ISettingsRepo
import com.hornedheck.midas.domain.usecase.GetHomeDashboardUseCase
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val getHomeDashboard: GetHomeDashboardUseCase,
    private val settingsRepo: ISettingsRepo,
) : ViewModel() {

    private val userSelectedRange = MutableStateFlow<HomeRange?>(null)

    private val effectiveRange = combine(
        userSelectedRange,
        settingsRepo.observeDashboardRange(),
    ) { userRange, settingsRange ->
        userRange ?: settingsRange.toHomeRange()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dashboardState = effectiveRange
        .flatMapLatest { range ->
            val today = currentDate()
            val (dateFrom, dateTo) = range.dateRange(today)
            val (prevFrom, prevTo) = range.previousDateRange(today)
            getHomeDashboard(dateFrom, dateTo, prevFrom, prevTo)
                .map { summary ->
                    if (summary.isEmpty) {
                        HomeUiState.Empty(range)
                    } else {
                        HomeUiState.Content(
                            selectedRange = range,
                            incomeCents = summary.incomeCents,
                            expensesCents = summary.expensesCents,
                            netBalanceCents = summary.netBalanceCents,
                            incomeDeltaPct = summary.incomeDeltaPct,
                            isIncomeTrendPositive = summary.incomeDeltaPct?.let { it >= 0f },
                            expensesDeltaPct = summary.expensesDeltaPct,
                            isExpensesTrendPositive = summary.expensesDeltaPct?.let { it <= 0f },
                            netBalanceDeltaPct = summary.netBalanceDeltaPct,
                            isNetBalanceTrendPositive = summary.netBalanceDeltaPct?.let { it >= 0f },
                            categories = summary.categories,
                            currencyCode = Currency.EUR.code,
                        )
                    }
                }
                .catch { emit(HomeUiState.Error(range)) }
        }

    val state: StateFlow<HomeUiState> = combine(
        dashboardState,
        settingsRepo.observeCurrency(),
    ) { uiState, currency ->
        if (uiState is HomeUiState.Content) uiState.copy(currencyCode = currency.code) else uiState
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        HomeUiState.Loading,
    )

    fun selectRange(range: HomeRange) {
        userSelectedRange.value = range
    }
}

private fun DashboardRange.toHomeRange(): HomeRange = when (this) {
    DashboardRange.ONE_MONTH -> HomeRange.ONE_MONTH
    DashboardRange.THREE_MONTHS -> HomeRange.THREE_MONTHS
    DashboardRange.SIX_MONTHS -> HomeRange.SIX_MONTHS
    DashboardRange.ONE_YEAR -> HomeRange.ONE_YEAR
}
