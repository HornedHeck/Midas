package com.hornedheck.midas.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.auth.BiometricAuthenticator
import com.hornedheck.midas.auth.BiometricResult
import com.hornedheck.midas.domain.repository.IAuthRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LockViewModel(
    private val authRepo: IAuthRepo,
    private val biometric: BiometricAuthenticator,
    private val lockHolder: AppLockHolder,
) : ViewModel() {

    private val _state = MutableStateFlow(LockState())
    val state: StateFlow<LockState> = _state.asStateFlow()

    init {
        lockHolder.locked.onEach { locked ->
            _state.update { it.copy(locked = locked) }
        }.launchIn(viewModelScope)

        lockHolder.ready.onEach { ready ->
            _state.update { it.copy(ready = ready) }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            val config = authRepo.observeAuthConfig().first()
            val available = config.biometricEnabled && biometric.isAvailable
            _state.update { it.copy(biometricAvailable = available) }
            if (available) authenticate()
        }
    }

    fun onEnterBackground() = lockHolder.onEnterBackground()
    fun onEnterForeground() = lockHolder.onEnterForeground()

    fun onDigit(digit: Char) {
        val current = _state.value
        if (current.input.length >= PIN_LENGTH) return
        val newInput = current.input + digit
        _state.update { it.copy(input = newInput, error = false) }
        if (newInput.length == PIN_LENGTH) submit(newInput)
    }

    fun onDelete() {
        _state.update { it.copy(input = it.input.dropLast(1), error = false) }
    }

    fun requestBiometric() {
        viewModelScope.launch { authenticate() }
    }

    private suspend fun authenticate() {
        if (biometric.authenticate() is BiometricResult.Success) lockHolder.unlock()
    }

    private fun submit(pin: String) {
        viewModelScope.launch {
            if (authRepo.verifyPin(pin)) {
                lockHolder.unlock()
                _state.update { it.copy(input = "") }
            } else {
                _state.update { it.copy(input = "", error = true) }
            }
        }
    }
}
