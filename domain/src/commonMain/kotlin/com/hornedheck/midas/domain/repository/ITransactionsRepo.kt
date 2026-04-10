package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.Transaction

interface ITransactionsRepo {
    suspend fun getTransactions(): List<Transaction>
}
