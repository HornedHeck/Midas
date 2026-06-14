package com.hornedheck.midas.ui.lock

import com.hornedheck.midas.domain.repository.IAuthRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

class AppLockHolder(
    authRepo: IAuthRepo,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    private val gracePeriod: Duration = 5.minutes,
) {

    private val scope = CoroutineScope(SupervisorJob() + coroutineContext)

    private val _unlocked = MutableStateFlow(false)
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    val locked: StateFlow<Boolean> = combine(
        authRepo.observeAuthConfig(),
        _unlocked,
    ) { config, unlocked ->
        _ready.value = true
        config.isAnyAuthEnabled && !unlocked
    }.stateIn(scope, SharingStarted.Eagerly, initialValue = true)

    private var backgroundMark: TimeSource.Monotonic.ValueTimeMark? = null

    fun lock() {
        _unlocked.value = false
    }

    fun unlock() {
        _unlocked.value = true
    }

    fun onEnterBackground() {
        if (!locked.value) backgroundMark = TimeSource.Monotonic.markNow()
    }

    fun onEnterForeground() {
        val mark = backgroundMark ?: return
        backgroundMark = null
        if (mark.elapsedNow() > gracePeriod) lock()
    }
}
