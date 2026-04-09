package com.hornedheck.midas
import com.hornedheck.midas.data.db.dbModule

import com.hornedheck.midas.data.dataModules
import com.hornedheck.midas.ui.uiModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

expect val platformModule: Module



val commonModule = module {
    includes(platformModule, uiModule, dataModules, dbModule)
}

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        includes(config)
        modules(commonModule)
    }
}
