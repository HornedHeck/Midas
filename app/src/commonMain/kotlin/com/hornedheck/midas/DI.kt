package com.hornedheck.midas

import com.hornedheck.midas.ui.uiModule
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule : Module

val modules = module {
    includes(platformModule, uiModule)
}
