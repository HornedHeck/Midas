package com.hornedheck.midas

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.hornedheck.midas.theme.MidasAppTheme
import com.hornedheck.midas.ui.auth.Auth
import com.hornedheck.midas.ui.main.Main
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI


@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    MidasAppTheme {
        val backStack = rememberNavBackStack(koinInject<SavedStateConfiguration>(), Main.TransactionsList)
        NavDisplay(
            backStack = backStack,
            entryProvider = koinEntryProvider<NavKey>()
        )
    }
}
