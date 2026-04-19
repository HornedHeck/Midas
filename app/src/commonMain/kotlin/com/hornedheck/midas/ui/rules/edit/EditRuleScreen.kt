package com.hornedheck.midas.ui.rules.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.domain.model.RuleType
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.ui.transaction.add.CategoryOption
import midas.app.generated.resources.Res
import midas.app.generated.resources.action_save
import midas.app.generated.resources.cd_back
import midas.app.generated.resources.hint_none
import midas.app.generated.resources.label_rule_amount_from
import midas.app.generated.resources.label_rule_amount_to
import midas.app.generated.resources.label_rule_target_category
import midas.app.generated.resources.label_rule_type
import midas.app.generated.resources.label_rule_value
import midas.app.generated.resources.rule_type_amount_range
import midas.app.generated.resources.rule_type_regex
import midas.app.generated.resources.rule_type_text_contains
import midas.app.generated.resources.rule_type_text_equals
import midas.app.generated.resources.screen_add_rule
import midas.app.generated.resources.screen_edit_rule
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EditRuleScreen(
    ruleId: Long?,
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    viewModel: EditRuleViewModel = koinViewModel(parameters = { parametersOf(ruleId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.status) {
        if (state.status is EditRuleStatus.Success) {
            onSaved()
            viewModel.clearSuccess()
        }
    }

    EditRuleScreen(
        state = state,
        isEditMode = ruleId != null,
        onBack = onBack,
        onRuleTypeSelected = viewModel::selectRuleType,
        onCategorySelected = viewModel::selectCategory,
        onSave = viewModel::save,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRuleScreen(
    state: EditRuleState,
    isEditMode: Boolean = false,
    onBack: () -> Unit = {},
    onRuleTypeSelected: (RuleType) -> Unit = {},
    onCategorySelected: (Long?) -> Unit = {},
    onSave: () -> Unit = {},
) {
    val isLoading = state.status is EditRuleStatus.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (isEditMode) Res.string.screen_edit_rule else Res.string.screen_add_rule))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.cd_back))
                    }
                },
            )
        },
        bottomBar = { SaveButton(isLoading = isLoading, onSave = onSave) },
    ) { paddingValues ->
        EditRuleFormContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            form = state.form,
            enabled = !isLoading,
            onRuleTypeSelected = onRuleTypeSelected,
            onCategorySelected = onCategorySelected,
        )
    }
}

@Composable
private fun SaveButton(isLoading: Boolean, onSave: () -> Unit) {
    Box(modifier = Modifier.padding(AppDimens.spacing4x)) {
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(AppDimens.spacing4x),
                    strokeWidth = AppDimens.spacing1x,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(Res.string.action_save))
            }
        }
    }
}

@Composable
private fun EditRuleFormContent(
    modifier: Modifier,
    form: EditRuleFormData,
    enabled: Boolean,
    onRuleTypeSelected: (RuleType) -> Unit,
    onCategorySelected: (Long?) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(AppDimens.spacing4x),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing3x),
    ) {
        RuleTypeSelector(
            selected = form.ruleType,
            enabled = enabled,
            onSelected = onRuleTypeSelected,
        )

        if (form.ruleType == RuleType.AMOUNT_RANGE) {
            AmountRangeFields(
                fromState = form.amountFromState,
                toState = form.amountToState,
                error = form.valueError,
                enabled = enabled,
            )
        } else {
            ValueField(
                state = form.valueState,
                error = form.valueError,
                enabled = enabled,
            )
        }

        CategoryDropdown(
            categories = form.categories,
            selectedId = form.selectedCategoryId,
            enabled = enabled,
            onCategorySelected = onCategorySelected,
        )
    }
}

@Composable
private fun RuleTypeSelector(
    selected: RuleType,
    enabled: Boolean,
    onSelected: (RuleType) -> Unit,
) {
    Column {
        Text(
            text = stringResource(Res.string.label_rule_type),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = AppDimens.spacing2x),
        )
        RuleType.entries.forEach { ruleType ->
            RuleTypeOption(
                ruleType = ruleType,
                selected = selected == ruleType,
                enabled = enabled,
                onSelected = onSelected,
            )
        }
    }
}

@Composable
private fun RuleTypeOption(
    ruleType: RuleType,
    selected: Boolean,
    enabled: Boolean,
    onSelected: (RuleType) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = { onSelected(ruleType) },
            enabled = enabled,
        )
        Text(
            text = stringResource(ruleType.labelRes()),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ValueField(state: TextFieldState, error: StringResource?, enabled: Boolean) {
    OutlinedTextField(
        state = state,
        label = { Text(stringResource(Res.string.label_rule_value)) },
        isError = error != null,
        supportingText = error?.let { res -> { Text(stringResource(res)) } },
        modifier = Modifier.fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        enabled = enabled,
    )
}

@Composable
private fun AmountRangeFields(
    fromState: TextFieldState,
    toState: TextFieldState,
    error: StringResource?,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing3x),
    ) {
        OutlinedTextField(
            state = fromState,
            label = { Text(stringResource(Res.string.label_rule_amount_from)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error != null,
            modifier = Modifier.weight(1f),
            lineLimits = TextFieldLineLimits.SingleLine,
            enabled = enabled,
        )
        OutlinedTextField(
            state = toState,
            label = { Text(stringResource(Res.string.label_rule_amount_to)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error != null,
            modifier = Modifier.weight(1f),
            lineLimits = TextFieldLineLimits.SingleLine,
            enabled = enabled,
        )
    }
    if (error != null) {
        Text(
            text = stringResource(error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<CategoryOption>,
    selectedId: Long?,
    enabled: Boolean,
    onCategorySelected: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.find { it.id == selectedId }?.name
        ?: stringResource(Res.string.hint_none)

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.label_rule_target_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded && enabled) },
            modifier = Modifier
                .fillMaxWidth()
                .exposedDropdownSize()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            enabled = enabled,
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.hint_none)) },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                },
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun RuleType.labelRes(): StringResource = when (this) {
    RuleType.TEXT_CONTAINS -> Res.string.rule_type_text_contains
    RuleType.TEXT_EQUALS -> Res.string.rule_type_text_equals
    RuleType.REGEX -> Res.string.rule_type_regex
    RuleType.AMOUNT_RANGE -> Res.string.rule_type_amount_range
}
