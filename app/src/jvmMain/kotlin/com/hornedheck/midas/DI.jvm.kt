package com.hornedheck.midas

import com.hornedheck.midas.auth.BiometricAuthenticator
import com.hornedheck.midas.auth.BiometricAuthenticatorJvm
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    single { BiometricAuthenticatorJvm() } bind BiometricAuthenticator::class
}
