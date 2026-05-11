package com.hornedheck.midas.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hornedheck.midas.domain.model.settings.AppTheme
import com.hornedheck.midas.domain.model.settings.Currency
import com.hornedheck.midas.domain.model.settings.DashboardRange
import com.hornedheck.midas.domain.repository.ISettingsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepo(
    private val dataStore: DataStore<Preferences>,
) : ISettingsRepo {

    override fun observeTheme(): Flow<AppTheme> =
        dataStore.data.map { prefs ->
            prefs[KEY_THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() }
                ?: AppTheme.DARK
        }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[KEY_THEME] = theme.name }
    }

    override fun observeDashboardRange(): Flow<DashboardRange> =
        dataStore.data.map { prefs ->
            prefs[KEY_DASHBOARD_RANGE]?.let { runCatching { DashboardRange.valueOf(it) }.getOrNull() }
                ?: DashboardRange.THREE_MONTHS
        }

    override suspend fun setDashboardRange(range: DashboardRange) {
        dataStore.edit { it[KEY_DASHBOARD_RANGE] = range.name }
    }

    override fun observeCurrency(): Flow<Currency> =
        dataStore.data.map { prefs ->
            prefs[KEY_CURRENCY]?.let { runCatching { Currency.valueOf(it) }.getOrNull() }
                ?: Currency.EUR
        }

    override suspend fun setCurrency(currency: Currency) {
        dataStore.edit { it[KEY_CURRENCY] = currency.name }
    }

    companion object {
        private val KEY_THEME = stringPreferencesKey("settings_theme")
        private val KEY_DASHBOARD_RANGE = stringPreferencesKey("settings_dashboard_range")
        private val KEY_CURRENCY = stringPreferencesKey("settings_currency")
    }
}
