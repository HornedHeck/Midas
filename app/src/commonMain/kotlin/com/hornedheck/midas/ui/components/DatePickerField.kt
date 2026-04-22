package com.hornedheck.midas.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.hornedheck.midas.util.format
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import midas.app.generated.resources.Res
import midas.app.generated.resources.action_cancel
import midas.app.generated.resources.action_ok
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
internal fun DatePickerField(
    date: LocalDate?,
    label: String,
    onDateChanged: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    clearable: Boolean = true,
) {
    var showPicker by remember { mutableStateOf(false) }

    DialogPickerField(
        value = date.format(),
        onClick = { showPicker = true },
        label = label,
        trailingIcon = {
            if (clearable && date != null) {
                IconButton(onClick = { onDateChanged(null) }) {
                    Icon(Icons.Outlined.Cancel, contentDescription = null)
                }
            } else {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
            }
        },
        modifier = modifier,
        enabled = enabled,
    )

    if (showPicker) {
        val focusManager = LocalFocusManager.current
        DatePickerFieldDialog(
            initialDate = date,
            onDateChanged = onDateChanged,
            onDismiss = {
                showPicker = false
                focusManager.clearFocus()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerFieldDialog(
    initialDate: LocalDate?,
    onDateChanged: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            ?.atStartOfDayIn(TimeZone.UTC)
            ?.toEpochMilliseconds(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant
                            .fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC)
                            .date
                        onDateChanged(selectedDate)
                    }
                    onDismiss()
                },
            ) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
