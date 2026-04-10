package com.hornedheck.midas.data.db

import app.cash.sqldelight.db.SqlDriver
import com.hornedheck.midas.db.Category
import com.hornedheck.midas.db.Database
import com.hornedheck.midas.db.Entry
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create

expect val driverModule: Module

internal val dbModule = module {
    includes(driverModule)
    single<Database> { create(::provideDatabase) }
}

private fun provideDatabase(driver: SqlDriver): Database {
    return Database(
        driver = driver,
        categoryAdapter = Category.Adapter(colorAdapter = ColorAdapter),
        entryAdapter = Entry.Adapter(
            datetimeAdapter = LocalDateTimeAdapter,
            category_sourceAdapter = CategorySourceAdapter,
        ),
    )
}
