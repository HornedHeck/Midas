package com.hornedheck.midas.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.settings.AppTheme
import com.hornedheck.midas.domain.model.settings.DashboardRange
import com.hornedheck.midas.domain.repository.ISettingsRepo
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepo: ISettingsRepo) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        settingsRepo.observeTheme(),
        settingsRepo.observeDashboardRange(),
    ) { theme, range ->
        SettingsState(selectedTheme = theme, dashboardRange = range)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        SettingsState(AppTheme.DARK, DashboardRange.THREE_MONTHS),
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepo.setTheme(theme) }
    }

    fun setDashboardRange(range: DashboardRange) {
        viewModelScope.launch { settingsRepo.setDashboardRange(range) }
    }
}
