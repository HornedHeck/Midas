package com.hornedheck.midas.data.db

import app.cash.sqldelight.ColumnAdapter
import com.hornedheck.midas.domain.model.CategorySource
import kotlinx.datetime.LocalDateTime

internal object CategorySourceAdapter : ColumnAdapter<CategorySource, Long> {
    override fun decode(databaseValue: Long): CategorySource =
        CategorySource.entries[databaseValue.toInt()]
    override fun encode(value: CategorySource): Long = value.ordinal.toLong()
}

internal object ColorAdapter : ColumnAdapter<Int, Long> {
    override fun decode(databaseValue: Long): Int = databaseValue.toInt()
    override fun encode(value: Int): Long = value.toLong()
}

internal object LocalDateTimeAdapter : ColumnAdapter<LocalDateTime, String> {
    override fun decode(databaseValue: String): LocalDateTime = LocalDateTime.parse(databaseValue)
    override fun encode(value: LocalDateTime): String = value.toString()
}
