package com.hornedheck.midas.ui.rules.edit

import androidx.compose.foundation.text.input.TextFieldState
import com.hornedheck.midas.domain.model.RuleType
import com.hornedheck.midas.ui.transaction.add.CategoryOption
import org.jetbrains.compose.resources.StringResource

data class EditRuleFormData(
    val ruleType: RuleType = RuleType.TEXT_CONTAINS,
    val valueState: TextFieldState = TextFieldState(),
    val amountFromState: TextFieldState = TextFieldState(),
    val amountToState: TextFieldState = TextFieldState(),
    val categories: List<CategoryOption> = emptyList(),
    val selectedCategoryId: Long? = null,
    val valueError: StringResource? = null,
)

sealed interface EditRuleStatus {
    data object Idle : EditRuleStatus
    data object Loading : EditRuleStatus
    data object Success : EditRuleStatus
    data class Error(val message: StringResource) : EditRuleStatus
}

data class EditRuleState(
    val form: EditRuleFormData = EditRuleFormData(),
    val status: EditRuleStatus = EditRuleStatus.Idle,
)
