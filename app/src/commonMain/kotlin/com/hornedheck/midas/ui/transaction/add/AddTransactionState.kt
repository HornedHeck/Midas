package com.hornedheck.midas.ui.transaction.add

import androidx.compose.foundation.text.input.TextFieldState
import com.hornedheck.midas.domain.model.CategorySource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource

data class CategoryOption(
    val id: Long,
    val name: String,
)

data class AddTransactionFormData(
    val isExpense: Boolean = true,
    val date: LocalDate,
    val originalTime: LocalTime? = null,
    val categories: List<CategoryOption> = emptyList(),
    val selectedCategoryId: Long? = null,
    val categorySource: CategorySource = CategorySource.AUTO,
    val amountError: StringResource? = null,
    val descriptionError: StringResource? = null,
    val amountState: TextFieldState = TextFieldState(initialText = "0.0"),
    val descriptionState: TextFieldState = TextFieldState(),
    val notesState: TextFieldState = TextFieldState(),
)

sealed interface SaveStatus {
    data object Idle : SaveStatus
    data object Loading : SaveStatus
    data object Success : SaveStatus
    data class Error(val message: StringResource) : SaveStatus
}

data class AddTransactionState(
    val form: AddTransactionFormData,
    val saveStatus: SaveStatus = SaveStatus.Idle,
)

