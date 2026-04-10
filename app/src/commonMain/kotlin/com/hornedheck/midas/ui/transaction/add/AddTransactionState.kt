package com.hornedheck.midas.ui.transaction.add

import kotlinx.datetime.LocalDate

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
    val descriptionError: String? = null,
    val amountError: String? = null,
    val generalError: String? = null,
)
