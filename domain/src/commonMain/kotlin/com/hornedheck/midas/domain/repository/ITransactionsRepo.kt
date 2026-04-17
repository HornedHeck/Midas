package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.Transaction
import com.hornedheck.midas.domain.model.TransactionDetails
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
    )

    suspend fun getTransactionById(id: Long): TransactionDetails?

    suspend fun updateTransaction(
        id: Long,
        datetime: LocalDateTime,
        amountCents: Long,
        description: String,
        categoryId: Long?,
        notes: String?,
    )

    suspend fun deleteTransaction(id: Long)
}
