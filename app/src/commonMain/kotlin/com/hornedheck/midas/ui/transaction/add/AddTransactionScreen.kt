package com.hornedheck.midas.ui.transaction.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.hornedheck.midas.util.formatDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import midas.app.generated.resources.Res
import midas.app.generated.resources.action_cancel
import midas.app.generated.resources.action_ok
import midas.app.generated.resources.action_save
import midas.app.generated.resources.cd_back
import midas.app.generated.resources.hint_none
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
                viewModel.clearSaved()
            }
            is SaveStatus.Error -> {
                snackbarHostState.showSnackbar(getString(status.message))
                viewModel.clearSaveError()
            }
            else -> Unit
        }
    }

    AddTransactionScreen(
        state = state,
        isEditMode = transactionId != null,
        onBack = onBack,
        onToggleType = viewModel::updateIsExpense,
        onDateChange = viewModel::updateDate,
        onCategoryChange = viewModel::updateCategory,
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
    onDateChange: (LocalDate) -> Unit = {},
    onCategoryChange: (String?) -> Unit = {},
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
private fun AddTransactionFormContent(
    modifier: Modifier,
    form: AddTransactionFormData,
    enabled: Boolean,
    onToggleType: (Boolean) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onCategoryChange: (String?) -> Unit,
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

        DateSection(date = form.date, enabled = enabled, onDateChange = onDateChange)

        CategoryDropdown(
            categories = form.categories,
            selectedId = form.selectedCategoryId,
            enabled = enabled,
            onCategorySelected = onCategoryChange,
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

private const val DECIMAL_PLACES = 2

private val DecimalInputTransformation = InputTransformation {
    val text = toString()
    val filtered = text.filter { it.isDigit() || it == '.' }

    val dotIndex = filtered.indexOf('.')
    val oneDot = if (dotIndex == -1) filtered
    else filtered.substring(0, dotIndex + 1) + filtered
        .substring(dotIndex + 1)
        .filter { it.isDigit() }

    val constrained = if (dotIndex != -1 && oneDot.length - dotIndex > DECIMAL_PLACES + 1) {
        oneDot.substring(0, dotIndex + DECIMAL_PLACES + 1)
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
    date: LocalDate,
    enabled: Boolean,
    onDateChange: (LocalDate) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    DateField(date = date, enabled = enabled, onPickerOpen = { showDatePicker = true })

    if (showDatePicker) {
        TransactionDatePickerDialog(
            currentDate = date,
            onDateSelected = { selected ->
                onDateChange(selected)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun DateField(
    date: LocalDate,
    enabled: Boolean,
    onPickerOpen: () -> Unit,
) {
    Box {
        OutlinedTextField(
            value = formatDate(date),
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
private fun CategoryDropdown(
    categories: List<CategoryOption>,
    selectedId: String?,
    enabled: Boolean,
    onCategorySelected: (String?) -> Unit,
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
            label = { Text(stringResource(Res.string.label_category)) },
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDatePickerDialog(
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate
            .atStartOfDayIn(TimeZone.UTC)
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
                            .toLocalDateTime(TimeZone.UTC).date
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
