package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.transaction.CategorySource
import com.hornedheck.midas.domain.model.dashboard.HomeDashboardData
import com.hornedheck.midas.domain.model.transaction.Transaction
import com.hornedheck.midas.domain.model.transaction.TransactionCategoryUpdate
import com.hornedheck.midas.domain.model.transaction.TransactionDetails
import com.hornedheck.midas.domain.model.transaction.TransactionFilter
import com.hornedheck.midas.domain.model.transaction.TransactionForApply
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ITransactionsRepo {
    fun getTransactions(filter: TransactionFilter? = null): Flow<List<Transaction>>

    fun observeHomeDashboard(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        prevFrom: LocalDate,
        prevTo: LocalDate,
    ): Flow<HomeDashboardData>

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

    suspend fun deleteAll()
}
