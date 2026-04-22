package com.hornedheck.midas.ui.transaction.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.domain.model.Category
import com.hornedheck.midas.domain.model.TransactionType
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.theme.MidasAppTheme
import com.hornedheck.midas.ui.components.AmountField
import com.hornedheck.midas.ui.components.DatePickerField
import com.hornedheck.midas.ui.components.DialogPickerField
import kotlinx.datetime.LocalDate
import midas.app.generated.resources.Res
import midas.app.generated.resources.action_apply
import midas.app.generated.resources.action_clear
import midas.app.generated.resources.cd_back
import midas.app.generated.resources.error_filter_invalid_amount_range
import midas.app.generated.resources.filter_type_both
import midas.app.generated.resources.filter_type_expense
import midas.app.generated.resources.filter_type_income
import midas.app.generated.resources.hint_none
import midas.app.generated.resources.hint_uncategorized
import midas.app.generated.resources.label_amount
import midas.app.generated.resources.label_categories
import midas.app.generated.resources.label_date
import midas.app.generated.resources.label_rule_amount_from
import midas.app.generated.resources.label_rule_amount_to
import midas.app.generated.resources.label_type
import midas.app.generated.resources.screen_configure_filters
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionFilterScreen(
    onBack: () -> Unit = {},
    viewModel: TransactionFilterViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.applied) {
        if (state.applied) {
            onBack()
            viewModel.clearApplied()
        }
    }

    TransactionFilterScreen(
        state = state,
        amountFromState = viewModel.amountFromState,
        amountToState = viewModel.amountToState,
        onBack = onBack,
        onUpdateType = viewModel::updateType,
        onUpdateDateFrom = viewModel::updateDateFrom,
        onUpdateDateTo = viewModel::updateDateTo,
        onApplyQuickRange = viewModel::applyQuickRange,
        onUpdateCategories = viewModel::updateSelectedCategories,
        onClearAll = viewModel::clearAll,
        onApply = viewModel::apply,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterScreen(
    state: TransactionFilterFormState,
    amountFromState: TextFieldState,
    amountToState: TextFieldState,
    onBack: () -> Unit = {},
    onUpdateType: (TransactionType) -> Unit = {},
    onUpdateDateFrom: (LocalDate?) -> Unit = {},
    onUpdateDateTo: (LocalDate?) -> Unit = {},
    onApplyQuickRange: (QuickDateRange) -> Unit = {},
    onUpdateCategories: (Set<Long?>) -> Unit = {},
    onClearAll: () -> Unit = {},
    onApply: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screen_configure_filters)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            FilterBottomBar(onClearAll = onClearAll, onApply = onApply)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppDimens.spacing4x)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4x),
        ) {
            TypeSection(type = state.type, onUpdateType = onUpdateType)
            AmountSection(
                amountFromState = amountFromState,
                amountToState = amountToState,
                hasError = state.amountError,
            )
            DateSection(
                dateFrom = state.dateFrom,
                dateTo = state.dateTo,
                selectedQuickRange = state.selectedQuickRange,
                onUpdateDateFrom = onUpdateDateFrom,
                onUpdateDateTo = onUpdateDateTo,
                onApplyQuickRange = onApplyQuickRange,
            )
            CategoriesSection(
                categories = state.categories,
                selectedIds = state.selectedCategoryIds,
                onUpdateCategories = onUpdateCategories,
            )
        }
    }
}

@Composable
private inline fun Section(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing2x),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        content()
    }
}

@Composable
private fun TypeSection(type: TransactionType, onUpdateType: (TransactionType) -> Unit) {
    Section(label = stringResource(Res.string.label_type)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            TransactionType.entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    selected = type == entry,
                    onClick = { onUpdateType(entry) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    icon = { SegmentedButtonDefaults.Icon(type == entry) },
                ) {
                    Text(
                        when (entry) {
                            TransactionType.ALL -> stringResource(Res.string.filter_type_both)
                            TransactionType.EXPENSES -> stringResource(Res.string.filter_type_expense)
                            TransactionType.INCOME -> stringResource(Res.string.filter_type_income)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountSection(
    amountFromState: TextFieldState,
    amountToState: TextFieldState,
    hasError: Boolean,
) {
    Section(label = stringResource(Res.string.label_amount)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing3x),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AmountField(
                state = amountFromState,
                label = Res.string.label_rule_amount_from,
                isError = hasError,
                modifier = Modifier.weight(1f),
            )
            AmountField(
                state = amountToState,
                label = Res.string.label_rule_amount_to,
                isError = hasError,
                modifier = Modifier.weight(1f),
            )
        }
        if (hasError) {
            Text(
                text = stringResource(Res.string.error_filter_invalid_amount_range),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DateSection(
    dateFrom: LocalDate?,
    dateTo: LocalDate?,
    selectedQuickRange: QuickDateRange?,
    onUpdateDateFrom: (LocalDate?) -> Unit,
    onUpdateDateTo: (LocalDate?) -> Unit,
    onApplyQuickRange: (QuickDateRange) -> Unit,
) {
    Section(
        label = stringResource(Res.string.label_date),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing4x),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DatePickerField(
                date = dateFrom,
                label = stringResource(Res.string.label_rule_amount_from),
                onDateChanged = onUpdateDateFrom,
                modifier = Modifier.weight(1f),
            )
            DatePickerField(
                date = dateTo,
                label = stringResource(Res.string.label_rule_amount_to),
                onDateChanged = onUpdateDateTo,
                modifier = Modifier.weight(1f),
            )
        }
        QuickRangesRow(
            selectedQuickRange = selectedQuickRange,
            onApplyQuickRange = onApplyQuickRange,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuickRangesRow(
    selectedQuickRange: QuickDateRange?,
    onApplyQuickRange: (QuickDateRange) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing2x),
        modifier = Modifier.fillMaxWidth()
    ) {
        QuickDateRange.entries.forEach { range ->
            OutlinedToggleButton(
                checked = selectedQuickRange == range,
                onCheckedChange = { onApplyQuickRange(range) },
                content = {
                    Text(stringResource(range.label))
                },
            )
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<Category>,
    selectedIds: Set<Long?>,
    onUpdateCategories: (Set<Long?>) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val uncategorized = stringResource(Res.string.hint_uncategorized)
    val selectedCategoriesLabel = when {
        selectedIds.isEmpty() -> stringResource(Res.string.hint_none)
        else -> selectedIds.take(MaxCategoryChips).joinToString(", ") { id ->
            if (id == null) uncategorized
            else categories.find { it.id == id }?.name ?: "?"
        } + if (selectedIds.size > MaxCategoryChips) " +${selectedIds.size - MaxCategoryChips}" else ""
    }

    Section(
        label = stringResource(Res.string.label_categories),
        modifier = Modifier.fillMaxWidth(),
    ) {
        DialogPickerField(
            value = selectedCategoriesLabel,
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                )
            },
        )

        if (showDialog) {
            TransactionFilterCategoryPickerDialog(
                categories = categories,
                selectedIds = selectedIds,
                onConfirm = { ids ->
                    onUpdateCategories(ids)
                    showDialog = false
                },
                onDismiss = { showDialog = false },
            )
        }
    }
}

@Composable
private fun FilterBottomBar(onClearAll: () -> Unit, onApply: () -> Unit) {
    val windowInsets = WindowInsets.systemBars.only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .padding(horizontal = AppDimens.spacing4x, vertical = AppDimens.spacing2x),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing4x),
    ) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onClearAll,
        ) {
            Text(stringResource(Res.string.action_clear))
        }
        Button(
            modifier = Modifier.weight(1f),
            onClick = onApply
        ) {
            Text(stringResource(Res.string.action_apply))
        }
    }
}

@Preview
@Composable
private fun TransactionFilterScreenPreview() {
    MidasAppTheme {
        TransactionFilterScreen(
            state = TransactionFilterFormState(),
            amountFromState = TextFieldState(),
            amountToState = TextFieldState(),
        )
    }
}

private const val MaxCategoryChips = 3
