package com.hornedheck.midas.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hornedheck.midas.data.model.CategoryRuleDto
import com.hornedheck.midas.data.model.toDomain
import com.hornedheck.midas.domain.model.CategoryRule
import com.hornedheck.midas.domain.model.RuleType
import com.hornedheck.midas.domain.repository.IRulesRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class RulesRepo(
    private val dataStore: DataStore<Preferences>,
) : IRulesRepo {

    private val json = Json { ignoreUnknownKeys = true }

    private val Flow<Preferences>.asRulesList: Flow<List<CategoryRuleDto>>
        get() = map { prefs ->
            prefs[RULES_KEY]?.let { json.decodeFromString(it) } ?: emptyList()
        }

    override fun getRules(): Flow<List<CategoryRule>> =
        dataStore.data.asRulesList.map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun getRuleById(id: Long): CategoryRule? =
        dataStore.data.asRulesList.first().firstOrNull { it.id == id }?.toDomain()

    override suspend fun countRulesForCategory(categoryId: Long): Int =
        dataStore.data.asRulesList.first().count { it.categoryId == categoryId }

    override suspend fun addRule(ruleType: RuleType, value: String, categoryId: Long?) {
        if (categoryId == null) return
        dataStore.edit { prefs ->
            val current: List<CategoryRuleDto> =
                prefs[RULES_KEY]?.let { json.decodeFromString(it) } ?: emptyList()
            val newId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
            val newPriority = (current.maxOfOrNull { it.priority } ?: -1) + 1
            val newRule = CategoryRuleDto(
                id = newId,
                ruleType = ruleType.name,
                value = value,
                priority = newPriority,
                categoryId = categoryId,
            )
            prefs[RULES_KEY] = json.encodeToString(current + newRule)
        }
    }

    override suspend fun updateRule(id: Long, ruleType: RuleType, value: String, categoryId: Long?) {
        if (categoryId == null) return
        dataStore.edit { prefs ->
            val current: List<CategoryRuleDto> =
                prefs[RULES_KEY]?.let { json.decodeFromString(it) } ?: emptyList()
            val updated = current.map { rule ->
                if (rule.id == id) rule.copy(ruleType = ruleType.name, value = value, categoryId = categoryId)
                else rule
            }
            prefs[RULES_KEY] = json.encodeToString(updated)
        }
    }

    override suspend fun deleteRule(id: Long) {
        dataStore.edit { prefs ->
            val current: List<CategoryRuleDto> =
                prefs[RULES_KEY]?.let { json.decodeFromString(it) } ?: emptyList()
            prefs[RULES_KEY] = json.encodeToString(current.filter { it.id != id })
        }
    }

    override suspend fun reorderRules(orderedIds: List<Long>) {
        dataStore.edit { prefs ->
            val current: List<CategoryRuleDto> =
                prefs[RULES_KEY]?.let { json.decodeFromString(it) } ?: emptyList()
            val idToRule = current.associateBy { it.id }
            val reordered = orderedIds.mapIndexedNotNull { index, id ->
                idToRule[id]?.copy(priority = index)
            }
            val orderedSet = orderedIds.toSet()
            val remaining = current.filter { it.id !in orderedSet }
            prefs[RULES_KEY] = json.encodeToString(reordered + remaining)
        }
    }

    companion object {
        private val RULES_KEY = stringPreferencesKey("rules")
    }
}
