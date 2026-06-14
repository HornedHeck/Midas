package com.hornedheck.midas.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import midas.app.generated.resources.Res
import midas.app.generated.resources.lock_title
import midas.app.generated.resources.lock_use_pin
import org.jetbrains.compose.resources.getString
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

class BiometricAuthenticatorAndroid(
    private val context: Context,
) : BiometricAuthenticator {

    private var activityRef: WeakReference<FragmentActivity>? = null

    fun bind(activity: FragmentActivity) { activityRef = WeakReference(activity) }
    fun unbind() { activityRef = null }

    override val isSupportedOnPlatform: Boolean = true

    override val isAvailable: Boolean
        get() = BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS

    override suspend fun authenticate(): BiometricResult {
        val activity = activityRef?.get() ?: return BiometricResult.Error(null)

        val title = getString(Res.string.lock_title)
        val negative = getString(Res.string.lock_use_pin)

        return suspendCancellableCoroutine { continuation ->
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) continuation.resume(BiometricResult.Success)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (continuation.isActive) continuation.resume(BiometricResult.Error(errString.toString()))
                    }
                },
            )
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setNegativeButtonText(negative)
                .setAllowedAuthenticators(BIOMETRIC_WEAK)
                .build()

            prompt.authenticate(info)
            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
        }
    }
}
