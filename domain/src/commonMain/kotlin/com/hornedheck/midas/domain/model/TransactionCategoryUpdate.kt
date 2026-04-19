package com.hornedheck.midas.domain.model

data class TransactionCategoryUpdate(
    val id: Long,
    val categoryId: Long?,
    val categorySource: CategorySource,
)
