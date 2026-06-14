package com.hornedheck.midas.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hornedheck.midas.domain.model.auth.AuthConfig
import com.hornedheck.midas.domain.repository.IAuthRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.kotlincrypto.hash.sha2.SHA256
import org.kotlincrypto.random.CryptoRand

class AuthRepo(
    private val dataStore: DataStore<Preferences>,
) : IAuthRepo {

    override fun observeAuthConfig(): Flow<AuthConfig> =
        dataStore.data.map { prefs ->
            AuthConfig(
                pinEnabled = prefs[KEY_PIN_ENABLED] == true,
                biometricEnabled = prefs[KEY_BIOMETRIC_ENABLED] == true,
                pinHash = prefs[KEY_PIN_HASH],
                pinSalt = prefs[KEY_PIN_SALT],
            )
        }

    override suspend fun enablePin(pin: String) {
        val salt = CryptoRand.Default.nextBytes(ByteArray(SALT_SIZE))
        val hash = hashPin(salt, pin)
        dataStore.edit { prefs ->
            prefs[KEY_PIN_ENABLED] = true
            prefs[KEY_PIN_HASH] = hash
            prefs[KEY_PIN_SALT] = salt.toHex()
        }
    }

    override suspend fun disablePin() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_PIN_ENABLED)
            prefs.remove(KEY_PIN_HASH)
            prefs.remove(KEY_PIN_SALT)
            prefs.remove(KEY_BIOMETRIC_ENABLED)
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val prefs = dataStore.data.first()
        val storedHash = prefs[KEY_PIN_HASH] ?: return false
        val salt = prefs[KEY_PIN_SALT]?.hexToBytes()
        return salt != null && constantTimeEquals(hashPin(salt, pin), storedHash)
    }

    private fun hashPin(salt: ByteArray, pin: String): String =
        SHA256().digest(salt + pin.encodeToByteArray()).toHex()

    private fun ByteArray.toHex(): String =
        joinToString("") { (it.toInt() and BYTE_MASK).toString(HEX_RADIX).padStart(2, '0') }

    private fun String.hexToBytes(): ByteArray =
        chunked(2).map { it.toInt(HEX_RADIX).toByte() }.toByteArray()

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].code xor b[i].code)
        return result == 0
    }

    companion object {
        private const val SALT_SIZE = 16
        private const val HEX_RADIX = 16
        private const val BYTE_MASK = 0xFF
        private val KEY_PIN_ENABLED = booleanPreferencesKey("settings_auth_pin_enabled")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("settings_auth_biometric_enabled")
        private val KEY_PIN_HASH = stringPreferencesKey("settings_auth_pin_hash")
        private val KEY_PIN_SALT = stringPreferencesKey("settings_auth_pin_salt")
    }
}
