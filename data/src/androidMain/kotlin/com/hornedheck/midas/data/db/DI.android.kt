package com.hornedheck.midas.data.db

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.hornedheck.midas.db.Database
import org.koin.core.module.Module
import org.koin.dsl.module

actual val driverModule: Module = module {
    single {
        AndroidSqliteDriver(Database.Schema, get(), "app.db").also { driver ->
            driver.execute(null, "PRAGMA foreign_keys = ON", 0)
        }
    }
}
