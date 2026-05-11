package com.hornedheck.midas.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.appVersion
import com.hornedheck.midas.domain.model.settings.AppTheme
import com.hornedheck.midas.domain.model.settings.Currency
import com.hornedheck.midas.domain.model.settings.DashboardRange
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.ui.navigation.BottomNavBar
import midas.app.generated.resources.Res
import midas.app.generated.resources.home_range_1m
import midas.app.generated.resources.home_range_1y
import midas.app.generated.resources.home_range_3m
import midas.app.generated.resources.home_range_6m
import midas.app.generated.resources.screen_settings
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
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onThemeSelected = viewModel::setTheme,
        onRangeSelected = viewModel::setDashboardRange,
        onCurrencySelected = viewModel::setCurrency,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingsState,
    onThemeSelected: (AppTheme) -> Unit,
    onRangeSelected: (DashboardRange) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.screen_settings)) })
        },
        bottomBar = { BottomNavBar() },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingsDropdownItem(
    label: String,
    selected: T,
    options: List<T>,
    onSelected: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.spacing4x, vertical = AppDimens.spacing3x),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Row(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing1x),
            ) {
                Text(
                    text = optionLabel(selected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                matchAnchorWidth = false
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
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
