package com.hornedheck.midas.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hornedheck.midas.theme.AppDimens
import midas.app.generated.resources.Res
import midas.app.generated.resources.cd_pin_delete
import org.jetbrains.compose.resources.stringResource

private val PinKeySize = 72.dp

@Composable
internal fun PinDots(filled: Int, error: Boolean, modifier: Modifier = Modifier) {
    val active = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing6x)
    ) {
        repeat(PIN_LENGTH) { index ->
            Box(
                modifier = Modifier
                    .size(AppDimens.spacing4x)
                    .clip(CircleShape)
                    .then(
                        if (index < filled) {
                            Modifier.background(active)
                        } else {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        },
                    ),
            )
        }
    }
}

@Composable
internal fun PinPad(onDigit: (Char) -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4x)
    ) {
        listOf("123", "456", "789").forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing4x)) {
                row.forEach { digit ->
                    PinKey(
                        label = digit.toString(),
                        onClick = { onDigit(digit) },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing4x)) {
            Spacer(Modifier.size(PinKeySize))
            PinKey(
                label = "0",
                onClick = { onDigit('0') },
            )
            PinKey(
                content = {
                    Icon(
                        Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = stringResource(Res.string.cd_pin_delete),
                    )
                },
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun PinKey(label: String, onClick: () -> Unit) {
    PinKey(
        content = {
            Text(
                label,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        onClick = onClick
    )
}

@Composable
private fun PinKey(content: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(PinKeySize)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
