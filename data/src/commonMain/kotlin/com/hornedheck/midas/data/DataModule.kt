package com.hornedheck.midas.data

import com.hornedheck.midas.data.db.dbModule
import com.hornedheck.midas.data.repository.CategoriesRepo
import com.hornedheck.midas.data.repository.TransactionsRepo
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import kotlin.coroutines.CoroutineContext

val dataModule = module {
    includes(dbModule)

    single<CoroutineContext> { create(::createDispatcher) }

    single<TransactionsRepo>() bind ITransactionsRepo::class
    single<CategoriesRepo>() bind ICategoriesRepo::class
}

fun createDispatcher() : CoroutineContext = Dispatchers.IO

