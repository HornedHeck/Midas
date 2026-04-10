package com.hornedheck.midas.data.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hornedheck.midas.db.Database
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.Properties

actual val driverModule: Module = module {
    single {
        JdbcSqliteDriver(
            JdbcSqliteDriver.IN_MEMORY,
            Properties(),
            Database.Schema.synchronous()
        ).also { driver ->
            driver.execute(null, "PRAGMA foreign_keys = ON", 0)
        }
    }
}
