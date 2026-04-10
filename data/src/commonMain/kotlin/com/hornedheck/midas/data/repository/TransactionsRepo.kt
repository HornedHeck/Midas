package com.hornedheck.midas.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.hornedheck.midas.db.Database
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import kotlin.coroutines.CoroutineContext

class TransactionsRepo(
    private val db: Database,
    private val ioContext: CoroutineContext,
) : ITransactionsRepo {

    override suspend fun getTransactions(): List<Transaction> = withContext(ioContext) {
        db.entryQueries.selectAll { id, datetime, amount, description, categoryName, categoryColor ->
            Transaction(
                id = id,
                datetime = datetime,
                amountCents = amount,
                description = description,
                categoryName = categoryName,
                categoryColor = categoryColor,
            )
        }.awaitAsList()
    }
}

