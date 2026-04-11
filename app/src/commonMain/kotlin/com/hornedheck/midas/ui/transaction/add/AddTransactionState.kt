package com.hornedheck.midas.ui.transaction.add

import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

data class CategoryOption(
    val id: String,
    val name: String,
)

data class AddTransactionFormState(
    val isExpense: Boolean = true,
    val amountText: String = "0.0",
    val description: String = "",
    val date: LocalDate,
    val categories: List<CategoryOption> = emptyList(),
    val selectedCategoryId: String? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val descriptionError: StringResource? = null,
    val amountError: StringResource? = null,
    val generalError: StringResource? = null,
)
