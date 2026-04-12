package com.hornedheck.midas.ui.transaction.add

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource

data class CategoryOption(
    val id: String,
    val name: String,
)

@Immutable
data class AddTransactionFormData(
    val isExpense: Boolean = true,
    val amountText: String = "0.0",
    val description: String = "",
    val date: LocalDate,
    val originalTime: LocalTime? = null,
    val categories: List<CategoryOption> = emptyList(),
    val selectedCategoryId: String? = null,
    val notes: String = "",
    val descriptionError: StringResource? = null,
    val amountError: StringResource? = null,
    val saveError: StringResource? = null,
)

sealed interface AddTransactionState {
    val form: AddTransactionFormData

    data class Editing(override val form: AddTransactionFormData) : AddTransactionState

    data class Saving(override val form: AddTransactionFormData) : AddTransactionState

    data class Saved(override val form: AddTransactionFormData) : AddTransactionState
}
