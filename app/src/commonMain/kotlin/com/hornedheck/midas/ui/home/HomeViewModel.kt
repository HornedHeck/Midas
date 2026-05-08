package com.hornedheck.midas.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.usecase.GetHomeDashboardUseCase
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val getHomeDashboard: GetHomeDashboardUseCase) : ViewModel() {

    private val selectedRange = MutableStateFlow(HomeRange.THREE_MONTHS)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeUiState> = selectedRange
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
                            incomeDelta = summary.incomeDelta,
                            expensesDelta = summary.expensesDelta,
                            netBalanceDelta = summary.netBalanceDelta,
                            categories = summary.categories,
                        )
                    }
                }
                .catch { emit(HomeUiState.Error(range)) }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
            HomeUiState.Loading,
        )

    fun selectRange(range: HomeRange) {
        selectedRange.value = range
    }
}
