package com.hornedheck.midas.data.model

import com.hornedheck.midas.domain.model.CategoryRule
import com.hornedheck.midas.domain.model.RuleType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryRuleDto(
    val id: Long,
    @SerialName("rule_type") val ruleType: String,
    val value: String,
    val priority: Int,
    @SerialName("category_id") val categoryId: Long,
)

fun CategoryRuleDto.toDomain(): CategoryRule = CategoryRule(
    id = id,
    ruleType = RuleType.valueOf(ruleType),
    value = value,
    priority = priority,
    categoryId = categoryId,
)

fun CategoryRule.toDto(): CategoryRuleDto = CategoryRuleDto(
    id = id,
    ruleType = ruleType.name,
    value = value,
    priority = priority,
    categoryId = categoryId,
)
