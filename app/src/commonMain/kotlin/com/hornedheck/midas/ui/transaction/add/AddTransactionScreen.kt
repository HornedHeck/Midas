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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.util.formatDate
import com.hornedheck.midas.theme.AppDimens
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

    LaunchedEffect(viewModel) {
        viewModel.savedEvent.collect { onBack() }
    }

    AddTransactionScreen(
        state = state,
        isEditMode = transactionId != null,
        onBack = onBack,
        onToggleType = viewModel::updateIsExpense,
        onAmountChange = viewModel::updateAmount,
        onDescriptionChange = viewModel::updateDescription,
        onDateChange = viewModel::updateDate,
        onCategoryChange = viewModel::updateCategory,
        onNotesChange = viewModel::updateNotes,
        onSave = viewModel::save,
        onClearError = viewModel::clearError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: AddTransactionFormState,
    isEditMode: Boolean = false,
    onBack: () -> Unit = {},
    onToggleType: (Boolean) -> Unit = {},
    onAmountChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onDateChange: (LocalDate) -> Unit = {},
    onCategoryChange: (String?) -> Unit = {},
    onNotesChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onClearError: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val generalErrorText = state.generalError?.let { stringResource(it) }

    LaunchedEffect(state.generalError) {
        generalErrorText?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }

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
        bottomBar = { SaveButton(isLoading = state.isLoading, onSave = onSave) },
    ) { paddingValues ->
        AddTransactionFormContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = state,
            onToggleType = onToggleType,
            onAmountChange = onAmountChange,
            onDescriptionChange = onDescriptionChange,
            onPickerOpen = { showDatePicker = true },
            onCategoryChange = onCategoryChange,
            onNotesChange = onNotesChange,
        )
    }

    if (showDatePicker) {
        TransactionDatePickerDialog(
            currentDate = state.date,
            onDateSelected = { date ->
                onDateChange(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
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
    modifier: Modifier = Modifier,
    state: AddTransactionFormState,
    onToggleType: (Boolean) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPickerOpen: () -> Unit,
    onCategoryChange: (String?) -> Unit,
    onNotesChange: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(AppDimens.spacing4x),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing3x),
    ) {
        TypeToggle(isExpense = state.isExpense, onToggle = onToggleType)

        AmountField(
            value = state.amountText,
            error = state.amountError,
            onValueChange = onAmountChange
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(Res.string.label_description)) },
            isError = state.descriptionError != null,
            supportingText = state.descriptionError?.let { res -> { Text(stringResource(res)) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        DateField(date = state.date, onPickerOpen = onPickerOpen)

        CategoryDropdown(
            categories = state.categories,
            selectedId = state.selectedCategoryId,
            onCategorySelected = onCategoryChange,
        )

        OutlinedTextField(
            value = state.notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(Res.string.label_notes)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeToggle(
    isExpense: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = isExpense,
            onClick = { onToggle(true) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = { SegmentedButtonDefaults.Icon(isExpense) },
        ) {
            Text(stringResource(Res.string.type_expense))
        }
        SegmentedButton(
            selected = !isExpense,
            onClick = { onToggle(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = { SegmentedButtonDefaults.Icon(!isExpense) },
        ) {
            Text(stringResource(Res.string.type_income))
        }
    }
}

@Composable
private fun AmountField(
    value: String,
    error: StringResource?,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() || it == '.' }
            if (filtered.count { it == '.' } <= 1) onValueChange(filtered)
        },
        label = { Text(stringResource(Res.string.label_amount)) },
        prefix = { Text("$") },
        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
        isError = error != null,
        supportingText = error?.let { res -> { Text(stringResource(res)) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun DateField(
    date: LocalDate,
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
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onPickerOpen),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<CategoryOption>,
    selectedId: String?,
    onCategorySelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.find { it.id == selectedId }?.name
        ?: stringResource(Res.string.hint_none)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.label_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .exposedDropdownSize()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
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
