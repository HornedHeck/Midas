package com.hornedheck.midas.data

import com.hornedheck.midas.data.db.dataStoreModule
import com.hornedheck.midas.data.db.dbModule
import com.hornedheck.midas.data.repository.CategoriesRepo
import com.hornedheck.midas.data.repository.RulesRepo
import com.hornedheck.midas.data.repository.TransactionsRepo
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.IRuleMatcher
import com.hornedheck.midas.domain.repository.IRulesRepo
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import com.hornedheck.midas.domain.repository.RuleMatcherImpl
import com.hornedheck.midas.domain.usecase.ApplyRulesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import kotlin.coroutines.CoroutineContext

val dataModule = module {
    includes(dbModule, dataStoreModule)

    single<CoroutineContext> { create(::createDispatcher) }

    single<TransactionsRepo>() bind ITransactionsRepo::class
    single<CategoriesRepo>() bind ICategoriesRepo::class
    single<RulesRepo>() bind IRulesRepo::class
    single<RuleMatcherImpl>() bind IRuleMatcher::class
    single<ApplyRulesUseCase>()
}

fun createDispatcher() : CoroutineContext = Dispatchers.IO

