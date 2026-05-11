package com.hornedheck.midas.domain.model.category

data class CategoryRule(
    val id: Long,
    val ruleType: RuleType,
    val value: String,
    val priority: Int,
    val categoryId: Long,
)
