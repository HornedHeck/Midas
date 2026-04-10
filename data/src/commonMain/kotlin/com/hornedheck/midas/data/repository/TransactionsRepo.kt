package com.hornedheck.midas.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.hornedheck.midas.data.db.model.CategorySource
import com.hornedheck.midas.db.Database
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TransactionsRepo(
    private val db: Database,
    private val ioContext: CoroutineContext,
) : ITransactionsRepo {

    private val _changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun changes(): Flow<Unit> = _changes.asSharedFlow()

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

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addTransaction(
        datetime: LocalDateTime,
        amountCents: Long,
        description: String,
        categoryId: String?,
        notes: String?,
    ) {
        withContext(ioContext) {
            db.entryQueries.insert(
                id = Uuid.random().toString(),
                datetime = datetime,
                amount = amountCents,
                description = description,
                categoryId = categoryId,
                notes = notes,
                categorySource = if (categoryId != null) CategorySource.MANUAL else CategorySource.NONE,
            )
            _changes.tryEmit(Unit)
            Unit
        }
    }
}


