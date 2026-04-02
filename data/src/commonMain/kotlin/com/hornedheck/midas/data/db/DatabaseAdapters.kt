package com.hornedheck.midas.data.db

import app.cash.sqldelight.ColumnAdapter
import com.hornedheck.midas.data.db.model.CategorySource
import com.hornedheck.midas.data.db.model.MatchType
import com.hornedheck.midas.data.db.model.RuleField

internal object CategorySourceAdapter : ColumnAdapter<CategorySource, Long> {
    override fun decode(databaseValue: Long): CategorySource =
        CategorySource.entries[databaseValue.toInt()]
    override fun encode(value: CategorySource): Long = value.ordinal.toLong()
}

internal object RuleFieldAdapter : ColumnAdapter<RuleField, Long> {
    override fun decode(databaseValue: Long): RuleField =
        RuleField.entries[databaseValue.toInt()]
    override fun encode(value: RuleField): Long = value.ordinal.toLong()
}

internal object MatchTypeAdapter : ColumnAdapter<MatchType, Long> {
    override fun decode(databaseValue: Long): MatchType =
        MatchType.entries[databaseValue.toInt()]
    override fun encode(value: MatchType): Long = value.ordinal.toLong()
}

internal object ColorAdapter : ColumnAdapter<Int, Long> {
    override fun decode(databaseValue: Long): Int = databaseValue.toInt()
    override fun encode(value: Int): Long = value.toLong()
}
