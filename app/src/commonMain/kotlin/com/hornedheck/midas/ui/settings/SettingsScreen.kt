package com.hornedheck.midas.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.appVersion
import com.hornedheck.midas.domain.model.settings.AppTheme
import com.hornedheck.midas.domain.model.settings.Currency
import com.hornedheck.midas.domain.model.settings.DashboardRange
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.ui.components.DeleteConfirmationDialog
import com.hornedheck.midas.ui.navigation.BottomNavBar
import midas.app.generated.resources.Res
import midas.app.generated.resources.dialog_clear_data_body
import midas.app.generated.resources.dialog_clear_data_title
import midas.app.generated.resources.dialog_clear_transactions_body
import midas.app.generated.resources.dialog_clear_transactions_title
import midas.app.generated.resources.error_clear_data_failed
import midas.app.generated.resources.error_clear_transactions_failed
import midas.app.generated.resources.home_range_1m
import midas.app.generated.resources.home_range_1y
import midas.app.generated.resources.home_range_3m
import midas.app.generated.resources.home_range_6m
import midas.app.generated.resources.screen_settings
import midas.app.generated.resources.settings_app_lock
import midas.app.generated.resources.settings_clear_data
import midas.app.generated.resources.settings_clear_transactions
import midas.app.generated.resources.settings_label_currency
import midas.app.generated.resources.settings_label_dashboard_range
import midas.app.generated.resources.settings_label_theme
import midas.app.generated.resources.settings_label_version
import midas.app.generated.resources.settings_theme_auto
import midas.app.generated.resources.settings_theme_dark
import midas.app.generated.resources.settings_theme_light
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onAppLockClick: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onThemeSelected = viewModel::setTheme,
        onRangeSelected = viewModel::setDashboardRange,
        onCurrencySelected = viewModel::setCurrency,
        onAppLockClick = onAppLockClick,
        onClearTransactions = viewModel::clearTransactions,
        onClearTransactionsStatusReset = viewModel::resetClearTransactionsStatus,
        onClearData = viewModel::clearAllData,
        onClearDataStatusReset = viewModel::resetClearDataStatus,
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingsState,
    onThemeSelected: (AppTheme) -> Unit,
    onRangeSelected: (DashboardRange) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    onAppLockClick: () -> Unit,
    onClearTransactions: () -> Unit,
    onClearTransactionsStatusReset: () -> Unit,
    onClearData: () -> Unit,
    onClearDataStatusReset: () -> Unit,
) {
    var showClearTransactionsDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.clearTransactionsStatus) {
        if (state.clearTransactionsStatus is ClearDataStatus.Success) {
            showClearTransactionsDialog = false
            onClearTransactionsStatusReset()
        }
    }
    LaunchedEffect(state.clearDataStatus) {
        if (state.clearDataStatus is ClearDataStatus.Success) {
            showClearDialog = false
            onClearDataStatusReset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.screen_settings)) })
        },
        bottomBar = { BottomNavBar() },
    ) { innerPadding ->
        SettingsContent(
            state = state,
            onThemeSelected = onThemeSelected,
            onRangeSelected = onRangeSelected,
            onCurrencySelected = onCurrencySelected,
            onAppLockClick = onAppLockClick,
            onClearTransactionsClick = { showClearTransactionsDialog = true },
            onClearDataClick = { showClearDialog = true },
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (showClearTransactionsDialog) {
        ClearDialog(
            clearStatus = state.clearTransactionsStatus,
            title = stringResource(Res.string.dialog_clear_transactions_title),
            body = stringResource(Res.string.dialog_clear_transactions_body),
            errorMessage = stringResource(Res.string.error_clear_transactions_failed),
            onDismiss = {
                showClearTransactionsDialog = false
                onClearTransactionsStatusReset()
            },
            onConfirm = onClearTransactions,
        )
    }
    if (showClearDialog) {
        ClearDialog(
            clearStatus = state.clearDataStatus,
            title = stringResource(Res.string.dialog_clear_data_title),
            body = stringResource(Res.string.dialog_clear_data_body),
            errorMessage = stringResource(Res.string.error_clear_data_failed),
            onDismiss = {
                showClearDialog = false
                onClearDataStatusReset()
            },
            onConfirm = onClearData,
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    onThemeSelected: (AppTheme) -> Unit,
    onRangeSelected: (DashboardRange) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    onAppLockClick: () -> Unit,
    onClearTransactionsClick: () -> Unit,
    onClearDataClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        SettingsDropdownItem(
            label = stringResource(Res.string.settings_label_theme),
            selected = state.selectedTheme,
            options = AppTheme.entries,
            onSelected = onThemeSelected,
            optionLabel = { it.label() },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))

        SettingsDropdownItem(
            label = stringResource(Res.string.settings_label_dashboard_range),
            selected = state.dashboardRange,
            options = DashboardRange.entries,
            onSelected = onRangeSelected,
            optionLabel = { it.label() },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))

        SettingsDropdownItem(
            label = stringResource(Res.string.settings_label_currency),
            selected = state.currency,
            options = Currency.entries,
            onSelected = onCurrencySelected,
            optionLabel = { it.code },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))

        SettingsNavItem(
            label = stringResource(Res.string.settings_app_lock),
            onClick = onAppLockClick,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))

        SettingsDangerItem(
            label = stringResource(Res.string.settings_clear_transactions),
            icon = Icons.Outlined.Delete,
            onClick = onClearTransactionsClick,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))

        SettingsDangerItem(
            label = stringResource(Res.string.settings_clear_data),
            icon = Icons.Outlined.ClearAll,
            onClick = onClearDataClick,
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(Res.string.settings_label_version, appVersion),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppDimens.spacing4x),
        )
    }
}

@Composable
private fun ClearDialog(
    clearStatus: ClearDataStatus,
    title: String,
    body: String,
    errorMessage: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = {
        if (clearStatus !is ClearDataStatus.Loading) onDismiss()
    }) {
        DeleteConfirmationDialog(
            title = title,
            body = body,
            errorMessage = if (clearStatus is ClearDataStatus.Error) errorMessage else null,
            isLoading = clearStatus is ClearDataStatus.Loading,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun AppTheme.label(): String = when (this) {
    AppTheme.AUTO -> stringResource(Res.string.settings_theme_auto)
    AppTheme.LIGHT -> stringResource(Res.string.settings_theme_light)
    AppTheme.DARK -> stringResource(Res.string.settings_theme_dark)
}

@Composable
private fun DashboardRange.label(): String = when (this) {
    DashboardRange.ONE_MONTH -> stringResource(Res.string.home_range_1m)
    DashboardRange.THREE_MONTHS -> stringResource(Res.string.home_range_3m)
    DashboardRange.SIX_MONTHS -> stringResource(Res.string.home_range_6m)
    DashboardRange.ONE_YEAR -> stringResource(Res.string.home_range_1y)
}
