package com.hornedheck.midas.ui.lock

const val PIN_LENGTH = 4

data class LockState(
    val ready: Boolean = false,
    val locked: Boolean = true,
    val input: String = "",
    val error: Boolean = false,
    val biometricAvailable: Boolean = false,
)
