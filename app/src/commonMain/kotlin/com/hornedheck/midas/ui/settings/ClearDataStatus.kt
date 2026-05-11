package com.hornedheck.midas.ui.settings

sealed interface ClearDataStatus {
    data object Idle : ClearDataStatus
    data object Loading : ClearDataStatus
    data object Success : ClearDataStatus
    data object Error : ClearDataStatus
}
