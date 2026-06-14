package com.hornedheck.midas.auth

class BiometricAuthenticatorJvm : BiometricAuthenticator {
    override val isSupportedOnPlatform: Boolean = false
    override val isAvailable: Boolean = false
    override suspend fun authenticate(): BiometricResult = BiometricResult.NotAvailable
}
