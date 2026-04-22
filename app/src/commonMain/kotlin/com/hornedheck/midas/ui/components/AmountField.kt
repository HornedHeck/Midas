package com.hornedheck.midas.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
internal fun AmountField(
    state: TextFieldState,
    label: StringResource,
    modifier: Modifier = Modifier,
    error: StringResource? = null,
    isError: Boolean = error != null,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        state = state,
        label = { Text(stringResource(label)) },
        modifier = modifier,
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        inputTransformation = DecimalInputTransformation,
        isError = isError,
        supportingText = error?.let { message -> { Text(stringResource(message)) } },
        enabled = enabled,
    )
}
