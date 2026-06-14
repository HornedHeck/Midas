package com.hornedheck.midas.auth

interface BiometricAuthenticator {
    val isSupportedOnPlatform: Boolean
    val isAvailable: Boolean
    suspend fun authenticate(): BiometricResult
}

sealed interface BiometricResult {
    data object Success : BiometricResult
    data object Failure : BiometricResult
    data object NotAvailable : BiometricResult
    data class Error(val message: String?) : BiometricResult
}
