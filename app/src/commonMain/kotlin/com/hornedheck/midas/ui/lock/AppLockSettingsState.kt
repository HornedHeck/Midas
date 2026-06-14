package com.hornedheck.midas.ui.lock

data class AppLockSettingsState(
    val pinEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricSupported: Boolean = false,
    val biometricAvailable: Boolean = false,
)
