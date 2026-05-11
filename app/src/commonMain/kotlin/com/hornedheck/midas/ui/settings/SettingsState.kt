package com.hornedheck.midas.ui.settings

import com.hornedheck.midas.domain.model.settings.AppTheme
import com.hornedheck.midas.domain.model.settings.Currency
import com.hornedheck.midas.domain.model.settings.DashboardRange

data class SettingsState(
    val selectedTheme: AppTheme,
    val dashboardRange: DashboardRange,
    val currency: Currency,
    val clearDataStatus: ClearDataStatus = ClearDataStatus.Idle,
    val clearTransactionsStatus: ClearDataStatus = ClearDataStatus.Idle,
)
