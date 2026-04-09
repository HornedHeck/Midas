package com.hornedheck.midas.data.db

import app.cash.sqldelight.db.SqlDriver
import com.hornedheck.midas.db.Database
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

expect val driverModule: Module

val dbModule = module {
    includes(driverModule)
    single { create(::provideDatabase) }
}

private fun provideDatabase(driver: SqlDriver): Database {
    return Database(driver)
}
