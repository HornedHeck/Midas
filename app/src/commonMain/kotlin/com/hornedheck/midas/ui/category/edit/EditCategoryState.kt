package com.hornedheck.midas.ui.category.edit

import androidx.compose.foundation.text.input.TextFieldState
import org.jetbrains.compose.resources.StringResource

internal val CATEGORY_PALETTE = listOf(
    0xFF4CAF50L.toInt(), // Green
    0xFF00BCD4L.toInt(), // Cyan
    0xFF9C27B0L.toInt(), // Purple
    0xFFEF9A9AL.toInt(), // Salmon
    0xFFBDBDBDL.toInt(), // Grey
    0xFFE91E63L.toInt(), // Pink
    0xFFCDDC39L.toInt(), // Lime
    0xFFFFC107L.toInt(), // Amber
    0xFF90CAF9L.toInt(), // Light Blue
    0xFF4DB6ACL.toInt(), // Teal
)

data class EditCategoryFormData(
    val nameState: TextFieldState = TextFieldState(),
    val selectedColor: Int = CATEGORY_PALETTE.first(),
    val nameError: StringResource? = null,
)

sealed interface EditCategoryStatus {
    data object Idle : EditCategoryStatus
    data object Loading : EditCategoryStatus
    data object Success : EditCategoryStatus
    data class Error(val message: StringResource) : EditCategoryStatus
}

data class EditCategoryState(
    val form: EditCategoryFormData = EditCategoryFormData(),
    val status: EditCategoryStatus = EditCategoryStatus.Idle,
)
