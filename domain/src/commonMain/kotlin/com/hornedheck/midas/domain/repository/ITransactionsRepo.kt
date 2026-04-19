package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.CategorySource
import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.model.TransactionCategoryUpdate
import com.hornedheck.midas.domain.model.TransactionDetails
import com.hornedheck.midas.domain.model.TransactionForApply
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface ITransactionsRepo {
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun addTransaction(
        datetime: LocalDateTime,
        amountCents: Long,
        description: String,
        categoryId: Long?,
        notes: String?,
        categorySource: CategorySource = CategorySource.MANUAL,
    )

    suspend fun getTransactionById(id: Long): TransactionDetails?

    suspend fun updateTransaction(
        id: Long,
        datetime: LocalDateTime,
        amountCents: Long,
        description: String,
        categoryId: Long?,
        notes: String?,
        categorySource: CategorySource = CategorySource.MANUAL,
    )

    suspend fun deleteTransaction(id: Long)

    suspend fun getTransactionsForAutoCategory(): List<TransactionForApply>

    suspend fun updateTransactionCategory(id: Long, categoryId: Long?, categorySource: CategorySource)

    suspend fun updateTransactionCategories(updates: List<TransactionCategoryUpdate>)
}
