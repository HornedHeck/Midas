package com.hornedheck.midas.data.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.hornedheck.midas.db.Database
import org.koin.core.module.Module
import org.koin.dsl.module

actual val driverModule: Module = module {
    single<SqlDriver> {
        AndroidSqliteDriver(Database.Schema.synchronous(), get(), "app.db").also { driver ->
            driver.execute(null, "PRAGMA foreign_keys = ON", 0)
        }
    }
}
