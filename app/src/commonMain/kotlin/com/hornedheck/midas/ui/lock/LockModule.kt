package com.hornedheck.midas.ui.lock

import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel

val lockModule = module {
    single<AppLockHolder>()
    viewModel<LockViewModel>()
    viewModel<AppLockSettingsViewModel>()
}
