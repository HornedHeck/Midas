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

fun formatDate(date: LocalDate): String =
    LocalDate.Format {
        monthName(MonthNames.ENGLISH_FULL)
        chars(" ")
        day()
        chars(", ")
        year()
    }.format(date)

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

@Suppress("MagicNumber")
fun parseAmountToCents(text: String): Long? {
    val cleaned = text.trim()
    if (cleaned.isEmpty()) return null
    val dotIndex = cleaned.indexOf('.')
    if (dotIndex == -1) {
        return cleaned.toLongOrNull()?.times(100)
    }
    val intStr = cleaned.substring(0, dotIndex)
    val fracStr = cleaned.substring(dotIndex + 1).take(2).padEnd(2, '0')
    val intPart = if (intStr.isEmpty()) 0L else intStr.toLongOrNull() ?: return null
    val fracPart = fracStr.toLongOrNull() ?: return null
    return intPart * 100 + fracPart
}

