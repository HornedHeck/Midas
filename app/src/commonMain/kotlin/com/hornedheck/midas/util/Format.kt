package com.hornedheck.midas.util

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
fun LocalDate?.format(): String {
    this ?: return ""
    val days = stringArrayResource(Res.array.days_of_week)
    val monthNames = stringArrayResource(Res.array.months)
    return remember(this, days, monthNames) {
        val formatter = LocalDate.Format {
            monthName(MonthNames(monthNames))
            chars(" ")
            day()
            chars(", ")
            year()
        }
        formatter.format(this)
    }
}

@Composable
fun LocalDate?.formatLong(): String {
    this ?: return ""
    val days = stringArrayResource(Res.array.days_of_week)
    val monthNames = stringArrayResource(Res.array.months)
    return remember(this, days, monthNames) {
        val formatter = LocalDate.Format {
            dayOfWeek(DayOfWeekNames(days))
            chars(", ")
            monthName(MonthNames(monthNames))
            chars(" ")
            day()
        }
        formatter.format(this)
    }
}

@Suppress("MagicNumber")
fun formatAmount(amountCents: Long, withCurrency: Boolean = false): String {
    val isExpense = amountCents < 0
    val absAmount = abs(amountCents)
    val dollars = absAmount / 100
    val cents = (absAmount % 100).toString().padStart(2, '0')
    return buildString {
        if (isExpense) append('-')
        append(dollars)
        append('.')
        append(cents)
        if (withCurrency)
            append(" USD")
    }
}

@Suppress("MagicNumber")
fun formatAbsAmount(amountCents: Long, withCurrency: Boolean = false): String {
    return formatAmount(abs(amountCents), withCurrency)
}
