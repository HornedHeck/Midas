package com.hornedheck.midas.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hornedheck.midas.db.Database
import com.hornedheck.midas.domain.model.CategorySource
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.model.TransactionCategoryUpdate
import com.hornedheck.midas.domain.model.TransactionDetails
import com.hornedheck.midas.domain.model.TransactionForApply
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlin.coroutines.CoroutineContext

class TransactionsRepo(
    private val db: Database,
    private val ioContext: CoroutineContext,
) : ITransactionsRepo {

    override fun getTransactions(): Flow<List<Transaction>> = db.entryQueries
        .selectAll { id, datetime, amount, description, categoryName, categoryColor ->
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
        categoryId: Long?,
        notes: String?,
        categorySource: CategorySource,
    ) {
        withContext(ioContext) {
            db.entryQueries.insert(
                datetime = datetime,
                amount = amountCents,
                description = description,
                categoryId = categoryId,
                notes = notes,
                categorySource = categorySource,
                isManual = if (categorySource == CategorySource.MANUAL) 1L else 0L,
            )
        }
    }

    override suspend fun getTransactionById(id: Long): TransactionDetails? =
        withContext(ioContext) {
            db.entryQueries
                .selectById(id) {
                        eId, datetime, amount, description, notes,
                        categoryId, categoryName, categoryColor, categorySource,
                    ->
                    TransactionDetails(
                        id = eId,
                        datetime = datetime,
                        amountCents = amount,
                        description = description,
                        notes = notes,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        categoryColor = categoryColor,
                        categorySource = categorySource,
                    )
                }
                .executeAsOneOrNull()
        }

    override suspend fun updateTransaction(
        id: Long,
        datetime: LocalDateTime,
        amountCents: Long,
        description: String,
        categoryId: Long?,
        notes: String?,
        categorySource: CategorySource,
    ) {
        withContext(ioContext) {
            db.entryQueries.update(
                id = id,
                datetime = datetime,
                amount = amountCents,
                description = description,
                categoryId = categoryId,
                notes = notes,
                categorySource = categorySource,
                isManual = if (categorySource == CategorySource.MANUAL) 1L else 0L,
            )
        }
    }

    override suspend fun deleteTransaction(id: Long) {
        withContext(ioContext) {
            db.entryQueries.deleteById(id)
        }
    }

    override suspend fun getTransactionsForAutoCategory(): List<TransactionForApply> =
        withContext(ioContext) {
            db.entryQueries
                .selectForReapply { id, datetime, amount, description, categoryId, categorySource ->
                    TransactionForApply(
                        id = id,
                        datetime = datetime,
                        amountCents = amount,
                        description = description,
                        categoryId = categoryId,
                        categorySource = categorySource,
                    )
                }
                .executeAsList()
        }

    override suspend fun updateTransactionCategory(
        id: Long,
        categoryId: Long?,
        categorySource: CategorySource,
    ) {
        withContext(ioContext) {
            db.entryQueries.updateCategoryById(
                categoryId = categoryId,
                categorySource = categorySource,
                isManual = if (categorySource == CategorySource.MANUAL) 1L else 0L,
                id = id,
            )
        }
    }

    override suspend fun updateTransactionCategories(updates: List<TransactionCategoryUpdate>) {
        withContext(ioContext) {
            db.entryQueries.transaction {
                updates.forEach { update ->
                    db.entryQueries.updateCategoryById(
                        categoryId = update.categoryId,
                        categorySource = update.categorySource,
                        isManual = if (update.categorySource == CategorySource.MANUAL) 1L else 0L,
                        id = update.id,
                    )
                }
            }
        }
    }
}
