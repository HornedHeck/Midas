package com.hornedheck.midas.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.settings.AppTheme
import com.hornedheck.midas.domain.model.settings.Currency
import com.hornedheck.midas.domain.model.settings.DashboardRange
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.IRulesRepo
import com.hornedheck.midas.domain.repository.ISettingsRepo
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepo: ISettingsRepo,
    private val transactionsRepo: ITransactionsRepo,
    private val categoriesRepo: ICategoriesRepo,
    private val rulesRepo: IRulesRepo,
) : ViewModel() {

    private val _clearDataStatus = MutableStateFlow<ClearDataStatus>(ClearDataStatus.Idle)
    private val _clearTransactionsStatus = MutableStateFlow<ClearDataStatus>(ClearDataStatus.Idle)

    val state: StateFlow<SettingsState> = combine(
        settingsRepo.observeTheme(),
        settingsRepo.observeDashboardRange(),
        settingsRepo.observeCurrency(),
        _clearDataStatus,
        _clearTransactionsStatus,
    ) { theme, range, currency, clearStatus, clearTransactionsStatus ->
        SettingsState(
            selectedTheme = theme,
            dashboardRange = range,
            currency = currency,
            clearDataStatus = clearStatus,
            clearTransactionsStatus = clearTransactionsStatus,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        SettingsState(AppTheme.DARK, DashboardRange.THREE_MONTHS, Currency.EUR),
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepo.setTheme(theme) }
    }

    fun setDashboardRange(range: DashboardRange) {
        viewModelScope.launch { settingsRepo.setDashboardRange(range) }
    }

    fun setCurrency(currency: Currency) {
        viewModelScope.launch { settingsRepo.setCurrency(currency) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _clearDataStatus.value = ClearDataStatus.Loading
            runCatching {
                transactionsRepo.deleteAll()
                categoriesRepo.deleteAll()
                rulesRepo.clearAll()
            }
                .onSuccess { _clearDataStatus.value = ClearDataStatus.Success }
                .onFailure { _clearDataStatus.value = ClearDataStatus.Error }
        }
    }

    fun resetClearDataStatus() {
        _clearDataStatus.value = ClearDataStatus.Idle
    }

    fun clearTransactions() {
        viewModelScope.launch {
            _clearTransactionsStatus.value = ClearDataStatus.Loading
            runCatching { transactionsRepo.deleteAll() }
                .onSuccess { _clearTransactionsStatus.value = ClearDataStatus.Success }
                .onFailure { _clearTransactionsStatus.value = ClearDataStatus.Error }
        }
    }

    fun resetClearTransactionsStatus() {
        _clearTransactionsStatus.value = ClearDataStatus.Idle
    }
}
