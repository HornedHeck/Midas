package com.hornedheck.midas.domain.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.CoroutineContext

class RuleMatcherImpl(
    rulesRepo: IRulesRepo,
) : IRuleMatcher {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val ruleSet: StateFlow<List<Rule>> = rulesRepo.getRules()
        .map { rules -> rules.map { it.toRule() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun match(description: String, amountCents: Long): Long? =
        ruleSet.value.firstNotNullOfOrNull { it(description, amountCents) }
}
