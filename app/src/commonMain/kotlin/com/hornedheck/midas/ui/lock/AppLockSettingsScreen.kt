package com.hornedheck.midas.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.ui.settings.SettingsNavItem
import com.hornedheck.midas.ui.settings.SettingsToggleItem
import midas.app.generated.resources.Res
import midas.app.generated.resources.auth_biometric_hardware_unavailable
import midas.app.generated.resources.auth_change_pin
import midas.app.generated.resources.auth_enable_biometric
import midas.app.generated.resources.auth_enable_pin
import midas.app.generated.resources.auth_setup_pin_confirm_title
import midas.app.generated.resources.auth_setup_pin_mismatch
import midas.app.generated.resources.auth_setup_pin_title
import midas.app.generated.resources.cd_back
import midas.app.generated.resources.screen_app_lock
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppLockSettingsScreen(
    onBack: () -> Unit = {},
    viewModel: AppLockSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AppLockSettingsScreen(
        state = state,
        onBack = onBack,
        onEnablePin = viewModel::enablePin,
        onDisablePin = viewModel::disablePin,
        onBiometricChange = viewModel::setBiometricEnabled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppLockSettingsScreen(
    state: AppLockSettingsState,
    onBack: () -> Unit,
    onEnablePin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onBiometricChange: (Boolean) -> Unit,
) {
    var showPinSetup by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screen_app_lock)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        AppLockSettingsContent(
            state = state,
            onPinToggle = { enabled -> if (enabled) showPinSetup = true else onDisablePin() },
            onChangePinClick = { showPinSetup = true },
            onBiometricChange = onBiometricChange,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
    }

    if (showPinSetup) {
        PinSetupDialog(
            onDismiss = { showPinSetup = false },
            onConfirmed = { pin ->
                onEnablePin(pin)
                showPinSetup = false
            },
        )
    }
}

@Composable
private fun AppLockSettingsContent(
    state: AppLockSettingsState,
    onPinToggle: (Boolean) -> Unit,
    onChangePinClick: () -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsToggleItem(
            label = stringResource(Res.string.auth_enable_pin),
            checked = state.pinEnabled,
            onCheckedChange = onPinToggle,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))

        if (state.pinEnabled) {
            SettingsNavItem(
                label = stringResource(Res.string.auth_change_pin),
                onClick = onChangePinClick,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))
        }

        if (state.biometricSupported) {
            SettingsToggleItem(
                label = stringResource(Res.string.auth_enable_biometric),
                checked = state.biometricEnabled,
                onCheckedChange = onBiometricChange,
                enabled = state.pinEnabled && state.biometricAvailable,
                note = if (!state.biometricAvailable) {
                    stringResource(Res.string.auth_biometric_hardware_unavailable)
                } else {
                    null
                },
            )
        }
    }
}

@Suppress("CognitiveComplexMethod")
@Composable
private fun PinSetupDialog(
    onDismiss: () -> Unit,
    onConfirmed: (String) -> Unit,
) {
    var firstPin by remember { mutableStateOf<String?>(null) }
    var input by remember { mutableStateOf("") }
    var mismatch by remember { mutableStateOf(false) }

    val confirming = firstPin != null

    fun onDigit(digit: Char) {
        if (input.length >= PIN_LENGTH) return
        mismatch = false
        input += digit
        if (input.length == PIN_LENGTH) {
            val entered = input
            if (!confirming) {
                firstPin = entered
                input = ""
            } else if (entered == firstPin) {
                onConfirmed(entered)
            } else {
                mismatch = true
                firstPin = null
                input = ""
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AppDimens.spacing1x,
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.spacing6x),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4x),
            ) {
                Text(
                    text = stringResource(
                        if (confirming) {
                            Res.string.auth_setup_pin_confirm_title
                        } else {
                            Res.string.auth_setup_pin_title
                        },
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (mismatch) {
                    Text(
                        text = stringResource(Res.string.auth_setup_pin_mismatch),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                PinDots(filled = input.length, error = mismatch)
                Spacer(Modifier.height(AppDimens.spacing2x))
                PinPad(onDigit = ::onDigit, onDelete = { input = input.dropLast(1) })
            }
        }
    }
}
