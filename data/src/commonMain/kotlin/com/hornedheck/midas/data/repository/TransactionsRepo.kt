package com.hornedheck.midas.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hornedheck.midas.db.Database
import com.hornedheck.midas.domain.model.CategorySource
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.model.TransactionCategoryUpdate
import com.hornedheck.midas.domain.model.TransactionDetails
import com.hornedheck.midas.domain.model.TransactionFilter
import com.hornedheck.midas.domain.model.TransactionForApply
import com.hornedheck.midas.domain.model.TransactionType
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.CoroutineContext

class TransactionsRepo(
    private val db: Database,
    private val ioContext: CoroutineContext,
) : ITransactionsRepo {

    override fun getTransactions(filter: TransactionFilter?): Flow<List<Transaction>> {
        val preparedFilter = PreparedTransactionsFilter.from(filter)
        return db.entryQueries
            .selectAll(
                include_expenses = preparedFilter.includeExpenses,
                include_income = preparedFilter.includeIncome,
                date_from = preparedFilter.dateFrom,
                date_to = preparedFilter.dateTo,
                amount_from_cents = preparedFilter.amountFromCents,
                amount_to_cents = preparedFilter.amountToCents,
                description_query = preparedFilter.descriptionQuery,
                apply_category_filter = preparedFilter.applyCategoryFilter,
                category_id = preparedFilter.categoryIds,
                include_uncategorized = preparedFilter.includeUncategorized,
            ) { id, datetime, amount, description, categoryId, categoryName, categoryColor ->
                Transaction(
                    id = id,
                    date = datetime,
                    amountCents = amount,
                    description = description,
                    categoryId = categoryId,
                    categoryName = categoryName,
                    categoryColor = categoryColor,
                )
            }
            .asFlow()
            .mapToList(ioContext)
    }

    override suspend fun upsertTransaction(
        id: Long?,
        date: LocalDate,
        amountCents: Long,
        description: String,
        categoryId: Long?,
        notes: String?,
        categorySource: CategorySource,
    ) {
        withContext(ioContext) {
            if (id == null) {
                db.entryQueries.insert(
                    datetime = date,
                    amount = amountCents,
                    description = description,
                    categoryId = categoryId,
                    notes = notes,
                    categorySource = categorySource,
                    isManual = categorySource.ordinal.toLong(),
                )
            } else {
                db.entryQueries.update(
                    id = id,
                    datetime = date,
                    amount = amountCents,
                    description = description,
                    categoryId = categoryId,
                    notes = notes,
                    categorySource = categorySource,
                    isManual = categorySource.ordinal.toLong(),
                )
            }
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
                        date = datetime,
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
                        date = datetime,
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
                isManual = categorySource.ordinal.toLong(),
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
                        isManual = update.categorySource.ordinal.toLong(),
                        id = update.id,
                    )
                }
            }
        }
    }

    private data class PreparedTransactionsFilter(
        val includeExpenses: Long,
        val includeIncome: Long,
        val dateFrom: LocalDate?,
        val dateTo: LocalDate?,
        val amountFromCents: Long?,
        val amountToCents: Long?,
        val descriptionQuery: String?,
        val applyCategoryFilter: Long,
        val categoryIds: List<Long>,
        val includeUncategorized: Long,
    ) {
        companion object {
            private const val FALSE = 0L
            private const val TRUE = 1L
            private const val UNUSED_CATEGORY_ID = -1L

            fun from(filter: TransactionFilter?): PreparedTransactionsFilter {
                val categoryIds = filter?.categoryIds.orEmpty()
                val selectedCategoryIds = categoryIds.filterNotNull()
                return PreparedTransactionsFilter(
                    includeExpenses = when (filter?.type ?: TransactionType.ALL) {
                        TransactionType.ALL, TransactionType.EXPENSES -> TRUE
                        TransactionType.INCOME -> FALSE
                    },
                    includeIncome = when (filter?.type ?: TransactionType.ALL) {
                        TransactionType.ALL, TransactionType.INCOME -> TRUE
                        TransactionType.EXPENSES -> FALSE
                    },
                    dateFrom = filter?.dateFrom,
                    dateTo = filter?.dateTo,
                    amountFromCents = filter?.amountFromCents,
                    amountToCents = filter?.amountToCents,
                    descriptionQuery = filter?.searchQuery
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?.lowercase()
                        ?.escapeLikePattern(),
                    applyCategoryFilter = if (categoryIds.isEmpty()) FALSE else TRUE,
                    categoryIds = selectedCategoryIds.ifEmpty { listOf(UNUSED_CATEGORY_ID) },
                    includeUncategorized = if (null in categoryIds) TRUE else FALSE,
                )
            }
        }
    }
}

private fun String.escapeLikePattern(): String =
    replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
