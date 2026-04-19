package com.hornedheck.midas.ui.transaction.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.ui.components.CategoryDropdown
import com.hornedheck.midas.ui.components.SaveButton
import com.hornedheck.midas.util.formatDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import midas.app.generated.resources.Res
import midas.app.generated.resources.action_cancel
import midas.app.generated.resources.action_ok
import midas.app.generated.resources.cd_back
import midas.app.generated.resources.label_amount
import midas.app.generated.resources.label_category
import midas.app.generated.resources.label_date
import midas.app.generated.resources.label_description
import midas.app.generated.resources.label_notes
import midas.app.generated.resources.screen_add_transaction
import midas.app.generated.resources.screen_edit_transaction
import midas.app.generated.resources.type_expense
import midas.app.generated.resources.type_income
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Instant

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit = {},
    transactionId: Long? = null,
    viewModel: AddTransactionViewModel = koinViewModel(parameters = { parametersOf(transactionId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saveStatus) {
        when (val status = state.saveStatus) {
            is SaveStatus.Success -> {
                onBack()
                viewModel.clearSaveStatus()
            }
            is SaveStatus.Error -> {
                snackbarHostState.showSnackbar(getString(status.message))
                viewModel.clearSaveStatus()
            }
            else -> Unit
        }
    }

    AddTransactionScreen(
        state = state,
        isEditMode = transactionId != null,
        onBack = onBack,
        onToggleType = viewModel::updateIsExpense,
        onDateChange = viewModel::updateDatetime,
        onCategoryChange = viewModel::updateCategory,
        onAutoCategory = viewModel::setAutoCategory,
        onSave = viewModel::save,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: AddTransactionState,
    isEditMode: Boolean = false,
    onBack: () -> Unit = {},
    onToggleType: (Boolean) -> Unit = {},
    onDateChange: (LocalDateTime) -> Unit = {},
    onCategoryChange: (Long?) -> Unit = {},
    onAutoCategory: () -> Unit = {},
    onSave: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val isLoading = state.saveStatus is SaveStatus.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEditMode) Res.string.screen_edit_transaction
                            else Res.string.screen_add_transaction
                        )
                    )
                },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { SaveButton(isLoading = isLoading, onSave = onSave) },
    ) { paddingValues ->
        AddTransactionFormContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            form = state.form,
            enabled = !isLoading,
            onToggleType = onToggleType,
            onDateChange = onDateChange,
            onCategoryChange = onCategoryChange,
            onAutoCategory = onAutoCategory,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionFormContent(
    modifier: Modifier,
    form: AddTransactionFormData,
    enabled: Boolean,
    onToggleType: (Boolean) -> Unit,
    onDateChange: (LocalDateTime) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onAutoCategory: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(AppDimens.spacing4x),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing3x),
    ) {
        TypeToggle(isExpense = form.isExpense, enabled = enabled, onToggle = onToggleType)

        AmountField(state = form.amountState, error = form.amountError, enabled = enabled)

        DescriptionField(
            state = form.descriptionState,
            error = form.descriptionError,
            enabled = enabled
        )

        DateSection(datetime = form.datetime, enabled = enabled, onDateChange = onDateChange)

        CategoryDropdown(
            categories = form.categories,
            selectedId = form.selectedCategoryId,
            categorySource = form.categorySource,
            enabled = enabled,
            label = Res.string.label_category,
            onCategorySelected = onCategoryChange,
            onAutoSelected = onAutoCategory,
        )

        OutlinedTextField(
            state = form.notesState,
            label = { Text(stringResource(Res.string.label_notes)) },
            modifier = Modifier.fillMaxWidth(),
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3),
            enabled = enabled,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeToggle(
    isExpense: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = isExpense,
            onClick = { onToggle(true) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = { SegmentedButtonDefaults.Icon(isExpense) },
            enabled = enabled,
        ) {
            Text(stringResource(Res.string.type_expense))
        }
        SegmentedButton(
            selected = !isExpense,
            onClick = { onToggle(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = { SegmentedButtonDefaults.Icon(!isExpense) },
            enabled = enabled,
        ) {
            Text(stringResource(Res.string.type_income))
        }
    }
}

private const val DecimalPlaces = 2

private val DecimalInputTransformation = InputTransformation {
    val text = toString()
    val filtered = text.filter { it.isDigit() || it == '.' }

    val dotIndex = filtered.indexOf('.')
    val oneDot = if (dotIndex == -1) filtered
    else filtered.substring(0, dotIndex + 1) + filtered
        .substring(dotIndex + 1)
        .filter { it.isDigit() }

    val constrained = if (dotIndex != -1 && oneDot.length - dotIndex > DecimalPlaces + 1) {
        oneDot.substring(0, dotIndex + DecimalPlaces + 1)
    } else {
        oneDot
    }

    if (constrained != text) replace(0, length, constrained)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountField(
    state: TextFieldState,
    error: StringResource?,
    enabled: Boolean,
) {
    OutlinedTextField(
        state = state,
        label = { Text(stringResource(Res.string.label_amount)) },
        prefix = { Text("$") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        inputTransformation = DecimalInputTransformation,
        isError = error != null,
        supportingText = error?.let { res -> { Text(stringResource(res)) } },
        modifier = Modifier.fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        enabled = enabled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DescriptionField(
    state: TextFieldState,
    error: StringResource?,
    enabled: Boolean,
) {
    OutlinedTextField(
        state = state,
        label = { Text(stringResource(Res.string.label_description)) },
        isError = error != null,
        supportingText = error?.let { res -> { Text(stringResource(res)) } },
        modifier = Modifier.fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        enabled = enabled,
    )
}

@Composable
private fun DateSection(
    datetime: LocalDateTime,
    enabled: Boolean,
    onDateChange: (LocalDateTime) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    DateField(datetime = datetime, enabled = enabled, onPickerOpen = { showDatePicker = true })

    if (showDatePicker) {
        TransactionDatePickerDialog(
            currentDatetime = datetime,
            onDateSelected = { selectedDate ->
                onDateChange(selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun DateField(
    datetime: LocalDateTime,
    enabled: Boolean,
    onPickerOpen: () -> Unit,
) {
    Box {
        OutlinedTextField(
            value = formatDate(datetime.date),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.label_date)) },
            trailingIcon = {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onPickerOpen, enabled = enabled),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDatePickerDialog(
    currentDatetime: LocalDateTime,
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDatetime
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant
                            .fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC)
                        onDateSelected(date)
                    } ?: onDismiss()
                },
            ) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
