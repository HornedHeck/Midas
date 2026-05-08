package com.hornedheck.midas.domain.usecase

import com.hornedheck.midas.domain.model.dashboard.CategorySpending
import com.hornedheck.midas.domain.model.dashboard.CategorySpendingSummary
import com.hornedheck.midas.domain.model.dashboard.HomeDashboardData
import com.hornedheck.midas.domain.model.dashboard.HomeDashboardSummary
import com.hornedheck.midas.domain.model.dashboard.TrendDelta
import com.hornedheck.midas.domain.model.dashboard.TrendDirection
import com.hornedheck.midas.domain.repository.ITransactionsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlin.math.abs

private const val MaxCategories = 8
private const val PercentageMultiplier = 100f

class GetHomeDashboardUseCase(private val repo: ITransactionsRepo) {

    operator fun invoke(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        prevFrom: LocalDate,
        prevTo: LocalDate,
    ): Flow<HomeDashboardSummary> =
        repo.observeHomeDashboard(dateFrom, dateTo, prevFrom, prevTo)
            .map { it.toSummary() }

    private fun HomeDashboardData.toSummary(): HomeDashboardSummary {
        val netCurrent = current.incomeCents - current.expensesCents
        val netPrevious = previous.incomeCents - previous.expensesCents
        val totalExpenses = breakdown.sumOf { it.totalCents }

        val processedCategories = groupCategories(breakdown, totalExpenses)

        return HomeDashboardSummary(
            incomeCents = current.incomeCents,
            expensesCents = current.expensesCents,
            netBalanceCents = netCurrent,
            incomeDelta = computeDelta(current.incomeCents, previous.incomeCents, upIsGood = true),
            expensesDelta = computeDelta(current.expensesCents, previous.expensesCents, upIsGood = false),
            netBalanceDelta = computeDelta(netCurrent, netPrevious, upIsGood = true),
            categories = processedCategories,
            isEmpty = current.incomeCents == 0L && current.expensesCents == 0L && breakdown.isEmpty(),
        )
    }

    private fun groupCategories(
        breakdown: List<CategorySpending>,
        totalExpenses: Long,
    ): List<CategorySpendingSummary> {
        if (breakdown.size <= MaxCategories) {
            return breakdown.map { it.toCategorySummary(totalExpenses) }
        }

        val top = breakdown.take(MaxCategories - 1)
        val othersTotal = breakdown.drop(MaxCategories - 1).sumOf { it.totalCents }

        return top.map { it.toCategorySummary(totalExpenses) } + CategorySpendingSummary(
            categoryId = null,
            name = null,
            color = null,
            totalCents = othersTotal,
            percentage = calculatePercentage(othersTotal, totalExpenses),
            isOthers = true,
        )
    }

    private fun calculatePercentage(part: Long, total: Long): Float =
        if (total > 0) part.toFloat() / total * PercentageMultiplier else 0f

    private fun CategorySpending.toCategorySummary(totalExpenses: Long) = CategorySpendingSummary(
        categoryId = categoryId,
        name = categoryName,
        color = categoryColor,
        totalCents = totalCents,
        percentage = calculatePercentage(totalCents, totalExpenses),
    )

    private fun computeDelta(current: Long, previous: Long, upIsGood: Boolean): TrendDelta? {
        if (previous == 0L) return null
        val pct = (current - previous).toFloat() / abs(previous).toFloat() * PercentageMultiplier
        val direction = if (pct >= 0f) {
            if (upIsGood) TrendDirection.POSITIVE else TrendDirection.NEGATIVE
        } else {
            if (upIsGood) TrendDirection.NEGATIVE else TrendDirection.POSITIVE
        }
        return TrendDelta(pct, direction)
    }
}
