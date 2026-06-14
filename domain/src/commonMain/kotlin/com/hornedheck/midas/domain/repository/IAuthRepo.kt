package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.auth.AuthConfig
import kotlinx.coroutines.flow.Flow

interface IAuthRepo {
    fun observeAuthConfig(): Flow<AuthConfig>
    suspend fun enablePin(pin: String)
    suspend fun disablePin()
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun verifyPin(pin: String): Boolean
}
