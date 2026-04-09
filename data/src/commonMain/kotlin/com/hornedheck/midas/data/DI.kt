package com.hornedheck.midas.data

import com.hornedheck.midas.data.db.dbModule
import org.koin.dsl.module

val dataModules = module {
    includes(dbModule)
}
