package com.hornedheck.midas.ui.home

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import midas.app.generated.resources.Res
import midas.app.generated.resources.home_range_1m
import midas.app.generated.resources.home_range_1y
import midas.app.generated.resources.home_range_3m
import midas.app.generated.resources.home_range_6m
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock

@Suppress("MagicNumber")
enum class HomeRange(
    val label: StringResource,
    private val months: Int,
) {

    ONE_MONTH(Res.string.home_range_1m, 1),
    THREE_MONTHS(Res.string.home_range_3m, 3),
    SIX_MONTHS(Res.string.home_range_6m, 6),
    ONE_YEAR(Res.string.home_range_1y, 12);

    fun dateRange(today: LocalDate = currentDate()): Pair<LocalDate, LocalDate> {
        val from = if (this == ONE_MONTH) today.firstDayOfCurrentMonth() else today.firstDayOfMonthsAgo(months)
        return from to today
    }

    fun previousDateRange(today: LocalDate = currentDate()): Pair<LocalDate, LocalDate> {
        val (from, _) = dateRange(today)
        val prevTo = from.minus(1, DateTimeUnit.DAY)
        return from.firstDayOfMonthsAgo(months) to prevTo
    }
}

internal fun currentDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

private fun LocalDate.firstDayOfCurrentMonth(): LocalDate = LocalDate(year, month, 1)

private fun LocalDate.firstDayOfMonthsAgo(months: Int): LocalDate =
    minus(months, DateTimeUnit.MONTH).let { LocalDate(it.year, it.month, 1) }
