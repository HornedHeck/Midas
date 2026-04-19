package com.hornedheck.midas.domain.usecase

import com.hornedheck.midas.domain.model.CategorySource
import com.hornedheck.midas.domain.model.TransactionCategoryUpdate
import com.hornedheck.midas.domain.repository.IRuleMatcher
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ApplyRulesUseCase(
    private val transactionsRepo: ITransactionsRepo,
    private val ruleMatcher: IRuleMatcher,
) {

    suspend operator fun invoke(): Int = coroutineScope {
        val transactions = transactionsRepo.getTransactionsForAutoCategory()

        val updates = transactions
            .map { transaction ->
                async {
                    val matchedCategoryId =
                        ruleMatcher.match(transaction.description, transaction.amountCents)
                    val newSource = CategorySource.AUTO
                    val changed = transaction.categoryId != matchedCategoryId
                        || transaction.categorySource != newSource
                    if (changed) TransactionCategoryUpdate(transaction.id, matchedCategoryId, newSource)
                    else null
                }
            }
            .awaitAll()
            .filterNotNull()

        if (updates.isNotEmpty()) {
            transactionsRepo.updateTransactionCategories(updates)
        }
        updates.size
    }
}
