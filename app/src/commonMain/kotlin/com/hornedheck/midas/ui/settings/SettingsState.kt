package com.hornedheck.midas.ui.settings

import com.hornedheck.midas.domain.model.settings.AppTheme
import com.hornedheck.midas.domain.model.settings.DashboardRange

data class SettingsState(
    val selectedTheme: AppTheme,
    val dashboardRange: DashboardRange,
)
