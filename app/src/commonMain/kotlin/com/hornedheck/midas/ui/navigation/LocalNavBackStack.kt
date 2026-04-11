package com.hornedheck.midas.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavKey

val LocalNavBackStack = compositionLocalOf<MutableList<NavKey>> {
    error("LocalNavBackStack not provided")
}
