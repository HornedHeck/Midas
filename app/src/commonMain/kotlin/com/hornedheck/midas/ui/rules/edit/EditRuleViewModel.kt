package com.hornedheck.midas.ui.rules.edit

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornedheck.midas.domain.model.RuleType
import com.hornedheck.midas.domain.repository.ICategoriesRepo
import com.hornedheck.midas.domain.repository.IRulesRepo
import com.hornedheck.midas.ui.transaction.add.CategoryOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import midas.app.generated.resources.Res
import midas.app.generated.resources.error_rule_invalid_amount_range
import midas.app.generated.resources.error_rule_invalid_regex
import midas.app.generated.resources.error_rule_value_required
import midas.app.generated.resources.error_save_rule_failed
import org.koin.core.annotation.InjectedParam

class EditRuleViewModel(
    @InjectedParam private val ruleId: Long?,
    private val rulesRepo: IRulesRepo,
    private val categoriesRepo: ICategoriesRepo,
) : ViewModel() {

    private val _state = MutableStateFlow(EditRuleState())
    val state: StateFlow<EditRuleState> = _state.asStateFlow()

    init {
        val form = _state.value.form
        viewModelScope.launch {
            snapshotFlow { form.valueState.text.toString() }
                .drop(1)
                .collectLatest { updateForm { copy(valueError = null) } }
        }
        loadCategories()
        ruleId?.let { loadRule(it) }
    }

    private fun loadRule(id: Long) {
        viewModelScope.launch {
            runCatching { rulesRepo.getRuleById(id) }
                .onSuccess { rule ->
                    rule?.let { r ->
                        val form = _state.value.form
                        if (r.ruleType == RuleType.AMOUNT_RANGE) {
                            val parts = r.value.split(":")
                            val fromText = parts.getOrElse(0) { "" }
                            val toText = parts.getOrElse(1) { "" }
                            form.amountFromState.edit { replace(0, length, fromText) }
                            form.amountToState.edit { replace(0, length, toText) }
                        } else {
                            form.valueState.edit { replace(0, length, r.value) }
                        }
                        updateForm {
                            copy(
                                ruleType = r.ruleType,
                                selectedCategoryId = r.categoryId,
                            )
                        }
                    }
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoriesRepo.getCategories().collect { categories ->
                updateForm {
                    copy(categories = categories.map { c -> CategoryOption(c.id, c.name) })
                }
            }
        }
    }

    private fun updateForm(transform: EditRuleFormData.() -> EditRuleFormData) {
        _state.update { current ->
            if (current.status is EditRuleStatus.Success) current
            else current.copy(form = current.form.transform())
        }
    }

    fun selectRuleType(ruleType: RuleType) {
        updateForm { copy(ruleType = ruleType, valueError = null) }
    }

    fun selectCategory(categoryId: Long?) {
        updateForm { copy(selectedCategoryId = categoryId) }
    }

    fun clearSuccess() {
        _state.update { it.copy(status = EditRuleStatus.Idle) }
    }

    fun save() {
        val current = _state.value
        if (current.status is EditRuleStatus.Loading || current.status is EditRuleStatus.Success) return
        if (!validate()) return

        val form = current.form
        val value = buildValue(form)

        viewModelScope.launch {
            _state.update { it.copy(status = EditRuleStatus.Loading) }
            runCatching {
                if (ruleId != null) {
                    rulesRepo.updateRule(
                        id = ruleId,
                        ruleType = form.ruleType,
                        value = value,
                        categoryId = form.selectedCategoryId,
                    )
                } else {
                    rulesRepo.addRule(
                        ruleType = form.ruleType,
                        value = value,
                        categoryId = form.selectedCategoryId,
                    )
                }
            }.onSuccess {
                _state.update { it.copy(status = EditRuleStatus.Success) }
            }.onFailure {
                _state.update { it.copy(status = EditRuleStatus.Error(Res.string.error_save_rule_failed)) }
            }
        }
    }

    private fun buildValue(form: EditRuleFormData): String =
        if (form.ruleType == RuleType.AMOUNT_RANGE) {
            val from = form.amountFromState.text.toString().trim()
            val to = form.amountToState.text.toString().trim()
            "$from:$to"
        } else {
            form.valueState.text.toString().trim()
        }

    private fun validate(): Boolean {
        val form = _state.value.form
        return if (form.ruleType == RuleType.AMOUNT_RANGE) {
            validateAmountRange(form)
        } else {
            validateTextValue(form)
        }
    }

    private fun validateTextValue(form: EditRuleFormData): Boolean {
        val text = form.valueState.text.toString().trim()
        val error = when {
            text.isEmpty() -> Res.string.error_rule_value_required
            form.ruleType == RuleType.REGEX && runCatching { Regex(text) }.isFailure ->
                Res.string.error_rule_invalid_regex
            else -> null
        }
        updateForm { copy(valueError = error) }
        return error == null
    }

    @Suppress("ReturnCount")
    private fun validateAmountRange(form: EditRuleFormData): Boolean {
        val fromText = form.amountFromState.text.toString().trim()
        val toText = form.amountToState.text.toString().trim()
        if (fromText.isEmpty() && toText.isEmpty()) {
            updateForm { copy(valueError = Res.string.error_rule_invalid_amount_range) }
            return false
        }
        val from = if (fromText.isNotEmpty()) fromText.toLongOrNull() else null
        val to = if (toText.isNotEmpty()) toText.toLongOrNull() else null
        if ((fromText.isNotEmpty() && from == null) || (toText.isNotEmpty() && to == null)) {
            updateForm { copy(valueError = Res.string.error_rule_invalid_amount_range) }
            return false
        }
        if (from != null && to != null && from > to) {
            updateForm { copy(valueError = Res.string.error_rule_invalid_amount_range) }
            return false
        }
        updateForm { copy(valueError = null) }
        return true
    }
}
