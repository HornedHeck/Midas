package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface ITransactionsRepo {
    suspend fun getTransactions(): List<Transaction>
    suspend fun addTransaction(
        datetime: LocalDateTime,
        amountCents: Long,
        description: String,
        categoryId: String?,
        notes: String?,
    )
    fun changes(): Flow<Unit>
}
