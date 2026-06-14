package com.hornedheck.midas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.hornedheck.midas.domain.model.settings.AppTheme
import com.hornedheck.midas.domain.repository.ISettingsRepo
import com.hornedheck.midas.theme.MidasAppTheme
import com.hornedheck.midas.ui.lock.LockScreen
import com.hornedheck.midas.ui.lock.LockViewModel
import com.hornedheck.midas.ui.main.Main
import com.hornedheck.midas.ui.navigation.LocalNavBackStack
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    val settingsRepo = koinInject<ISettingsRepo>()
    val theme by settingsRepo.observeTheme().collectAsStateWithLifecycle(AppTheme.DARK)

    val lockViewModel = koinViewModel<LockViewModel>()
    val lockState by lockViewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { lockViewModel.onEnterBackground() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { lockViewModel.onEnterForeground() }

    if (!lockState.ready) return

    MidasAppTheme(appTheme = theme) {
        if (lockState.locked) {
            LockScreen(
                state = lockState,
                onDigit = lockViewModel::onDigit,
                onDelete = lockViewModel::onDelete,
                onUseBiometric = lockViewModel::requestBiometric,
            )
        } else {
            val backStack = rememberNavBackStack(
                koinInject<SavedStateConfiguration>(),
                Main.Dashboard,
            )
            CompositionLocalProvider(LocalNavBackStack provides backStack) {
                NavDisplay(
                    backStack = backStack,
                    entryProvider = koinEntryProvider<NavKey>(),
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    sceneStrategy = DialogSceneStrategy(),
                )
            }
        }
    }
}
