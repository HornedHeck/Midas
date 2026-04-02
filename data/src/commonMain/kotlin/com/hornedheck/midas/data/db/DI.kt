package com.hornedheck.midas.data.db

import com.hornedheck.midas.db.Database
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

expect val driverModule: Module

val dbModule = module {
    includes(driverModule)
    single<Database>()
}
