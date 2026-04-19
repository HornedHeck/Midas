package com.hornedheck.midas.domain.repository

import com.hornedheck.midas.domain.model.CategoryRule
import com.hornedheck.midas.domain.model.RuleType
import kotlin.math.abs

interface IRuleMatcher {

    suspend fun match(description: String, amountCents: Long): Long?
}

fun interface Rule {

    operator fun invoke(description: String, amountCents: Long): Long?
}

fun CategoryRule.toRule(): Rule = Rule { description, amountCents ->
    val matches = when (ruleType) {
        RuleType.TEXT_CONTAINS -> description.contains(value, ignoreCase = true)
        RuleType.TEXT_EQUALS -> description.equals(value, ignoreCase = true)
        RuleType.REGEX -> runCatching { Regex(value).containsMatchIn(description) }.getOrElse { false }
        RuleType.AMOUNT_RANGE -> {
            val parts = value.split(":")
            if (parts.size != 2) return@Rule null
            val min = parts[0].toLongOrNull()
            val max = parts[1].toLongOrNull()
            val absAmount = abs(amountCents)
            (min == null || absAmount >= min) && (max == null || absAmount <= max)
        }
    }
    if (matches) categoryId else null
}
