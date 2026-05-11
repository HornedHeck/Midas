package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.settings.AppTheme
import com.hornedheck.midas.domain.model.settings.DashboardRange
import kotlinx.coroutines.flow.Flow

interface ISettingsRepo {
    fun observeTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
    fun observeDashboardRange(): Flow<DashboardRange>
    suspend fun setDashboardRange(range: DashboardRange)
}
