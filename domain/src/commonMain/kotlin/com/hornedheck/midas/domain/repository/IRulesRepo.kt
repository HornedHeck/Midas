package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.CategoryRule
import com.hornedheck.midas.domain.model.RuleType
import kotlinx.coroutines.flow.Flow

interface IRulesRepo {
    fun getRules(): Flow<List<CategoryRule>>
    suspend fun getRuleById(id: Long): CategoryRule?
    suspend fun countRulesForCategory(categoryId: Long): Int
    suspend fun addRule(ruleType: RuleType, value: String, categoryId: Long?)
    suspend fun updateRule(id: Long, ruleType: RuleType, value: String, categoryId: Long?)
    suspend fun deleteRule(id: Long)
    suspend fun reorderRules(orderedIds: List<Long>)
}
