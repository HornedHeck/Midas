package com.hornedheck.midas.domain.model.transaction

data class TransactionCategoryUpdate(
    val id: Long,
    val categoryId: Long?,
    val categorySource: CategorySource,
)
