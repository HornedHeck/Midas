package com.hornedheck.midas.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hornedheck.midas.theme.AppDimens
import midas.app.generated.resources.Res
import midas.app.generated.resources.action_save
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SaveButton(
    isLoading: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(AppDimens.spacing4x)) {
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
