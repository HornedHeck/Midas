package com.hornedheck.midas.domain.model.auth

data class AuthConfig(
    val pinEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val pinHash: String? = null,
    val pinSalt: String? = null,
) {
    val isAnyAuthEnabled: Boolean get() = pinEnabled || biometricEnabled
}
