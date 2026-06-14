package com.hornedheck.midas.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.auth.BiometricAuthenticator
import com.hornedheck.midas.domain.repository.IAuthRepo
import com.hornedheck.midas.util.SUBSCRIPTION_TIMEOUT
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppLockSettingsViewModel(
    private val authRepo: IAuthRepo,
    private val biometric: BiometricAuthenticator,
) : ViewModel() {

    val state: StateFlow<AppLockSettingsState> = authRepo.observeAuthConfig()
        .map { config ->
            AppLockSettingsState(
                pinEnabled = config.pinEnabled,
                biometricEnabled = config.biometricEnabled,
                biometricSupported = biometric.isSupportedOnPlatform,
                biometricAvailable = biometric.isAvailable,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
            AppLockSettingsState(biometricSupported = biometric.isSupportedOnPlatform),
        )

    fun enablePin(pin: String) {
        viewModelScope.launch { authRepo.enablePin(pin) }
    }

    fun disablePin() {
        viewModelScope.launch { authRepo.disablePin() }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { authRepo.setBiometricEnabled(enabled) }
    }
}
