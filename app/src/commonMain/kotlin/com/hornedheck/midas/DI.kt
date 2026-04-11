package com.hornedheck.midas

import com.hornedheck.midas.data.dataModule
import com.hornedheck.midas.ui.uiModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module

expect val platformModule: Module

val joinedModule = module {
    includes(platformModule, uiModule, dataModule)
}

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        includes(config)
        modules(joinedModule)
    }
}
