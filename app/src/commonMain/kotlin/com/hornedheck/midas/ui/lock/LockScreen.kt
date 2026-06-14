package com.hornedheck.midas.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.hornedheck.midas.theme.AppDimens
import midas.app.generated.resources.Res
import midas.app.generated.resources.lock_error
import midas.app.generated.resources.lock_subtitle
import midas.app.generated.resources.lock_title
import midas.app.generated.resources.lock_use_biometric
import org.jetbrains.compose.resources.stringResource

@Composable
fun LockScreen(
    state: LockState,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onUseBiometric: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.spacing6x),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(AppDimens.spacing10x),
            )
            Spacer(Modifier.height(AppDimens.spacing4x))
            Text(
                text = stringResource(if (state.error) Res.string.lock_error else Res.string.lock_title),
                style = MaterialTheme.typography.headlineSmall,
                color = if (state.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(AppDimens.spacing2x))
            Text(
                text = stringResource(Res.string.lock_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(AppDimens.spacing6x))
            PinDots(
                filled = state.input.length,
                error = state.error,
            )
            Spacer(Modifier.height(AppDimens.spacing8x))

            PinPad(
                onDigit = onDigit,
                onDelete = onDelete,
            )

            Spacer(Modifier.height(AppDimens.spacing4x))
            if (state.biometricAvailable) {
                TextButton(onClick = onUseBiometric) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                    )
                    Spacer(Modifier.size(AppDimens.spacing2x))
                    Text(stringResource(Res.string.lock_use_biometric))
                }
            }
        }
    }
}
