package com.hornedheck.midas.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hornedheck.midas.data.db.model.CategorySource
import com.hornedheck.midas.db.Database
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.model.TransactionDetails
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlin.coroutines.CoroutineContext

class TransactionsRepo(
    private val db: Database,
    private val ioContext: CoroutineContext,
) : ITransactionsRepo {

    override fun getTransactions(): Flow<List<Transaction>> =
        db.entryQueries.selectAll { id, datetime, amount, description, categoryName, categoryColor ->
            Transaction(
                id = id,
                datetime = datetime,
                amountCents = amount,
                description = description,
                categoryName = categoryName,
                categoryColor = categoryColor,
            )
        }
            .asFlow()
            .mapToList(ioContext)

    override suspend fun addTransaction(
        datetime: LocalDateTime,
        amountCents: Long,
        description: String,
        categoryId: String?,
        notes: String?,
    ) {
        withContext(ioContext) {
            db.entryQueries.insert(
                datetime = datetime,
                amount = amountCents,
                description = description,
                categoryId = categoryId,
                notes = notes,
                categorySource = if (categoryId != null) CategorySource.MANUAL else CategorySource.NONE,
            )
        }
    }

    override suspend fun getTransactionById(id: Long): TransactionDetails? =
        withContext(ioContext) {
            db.entryQueries.selectById(id) { eId, datetime, amount, description, notes,
                categoryId, categoryName, categoryColor ->
                TransactionDetails(
                    id = eId,
                    datetime = datetime,
                    amountCents = amount,
                    description = description,
                    notes = notes,
                    categoryId = categoryId,
                    categoryName = categoryName,
                    categoryColor = categoryColor,
                )
            }.executeAsOneOrNull()
        }

    override suspend fun updateTransaction(
        id: Long,
        datetime: LocalDateTime,
        amountCents: Long,
        description: String,
        categoryId: String?,
        notes: String?,
    ) {
        withContext(ioContext) {
            db.entryQueries.update(
                id = id,
                datetime = datetime,
                amount = amountCents,
                description = description,
                categoryId = categoryId,
                notes = notes,
                categorySource = if (categoryId != null) CategorySource.MANUAL else CategorySource.NONE,
            )
        }
    }
}
