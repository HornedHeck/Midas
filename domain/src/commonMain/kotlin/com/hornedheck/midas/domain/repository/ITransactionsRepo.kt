package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.CategorySource
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.model.TransactionCategoryUpdate
import com.hornedheck.midas.domain.model.TransactionDetails
import com.hornedheck.midas.domain.model.TransactionFilter
import com.hornedheck.midas.domain.model.TransactionForApply
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ITransactionsRepo {
    fun getTransactions(filter: TransactionFilter? = null): Flow<List<Transaction>>

    suspend fun upsertTransaction(
        id: Long?,
        date: LocalDate,
        amountCents: Long,
        description: String,
        categoryId: Long?,
        notes: String?,
        categorySource: CategorySource,
    )

    suspend fun getTransactionById(id: Long): TransactionDetails?

    suspend fun deleteTransaction(id: Long)

    suspend fun getTransactionsForAutoCategory(): List<TransactionForApply>

    suspend fun updateTransactionCategory(id: Long, categoryId: Long?, categorySource: CategorySource)

    suspend fun updateTransactionCategories(updates: List<TransactionCategoryUpdate>)
}
