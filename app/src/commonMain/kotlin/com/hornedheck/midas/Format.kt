package com.hornedheck.midas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import midas.app.generated.resources.Res
import midas.app.generated.resources.days_of_week
import midas.app.generated.resources.months
import org.jetbrains.compose.resources.stringArrayResource
import kotlin.math.abs

@Composable
fun formatDateHeader(date: LocalDate): String {
    val days = stringArrayResource(Res.array.days_of_week)
    val monthNames = stringArrayResource(Res.array.months)
    return remember(date, days, monthNames) {
        val formatter = LocalDate.Format {
            dayOfWeek(DayOfWeekNames(days))
            chars(", ")
            monthName(MonthNames(monthNames))
            chars(" ")
            day()
        }
        formatter.format(date)
    }
}

@Suppress("MagicNumber")
fun formatAmount(amountCents: Long): String {
    val isExpense = amountCents < 0
    val absAmount = abs(amountCents)
    val dollars = absAmount / 100
    val cents = (absAmount % 100).toString().padStart(2, '0')
    return buildString {
        if (isExpense) append('-')
        append(dollars)
        append('.')
        append(cents)
        append(" USD")
    }
}
