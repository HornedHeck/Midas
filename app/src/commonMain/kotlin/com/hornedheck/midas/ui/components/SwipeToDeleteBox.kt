package com.hornedheck.midas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import com.hornedheck.midas.theme.AppDimens
import midas.app.generated.resources.Res
import midas.app.generated.resources.cd_delete
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue

private const val SwipeProgressMultiple = 1.25f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val swipeState = rememberSwipeToDismissBoxState()
    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            swipeState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    var width by remember { mutableFloatStateOf(0f) }
    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            if (swipeState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.cd_delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            lerp(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.errorContainer,
                                swipeState.requireOffset().absoluteValue
                                    .times(SwipeProgressMultiple)
                                    .div(width)
                                    .coerceIn(0f, 1f),
                            )
                        )
                        .wrapContentSize(Alignment.CenterEnd)
                        .padding(AppDimens.spacing3x),
                )
            }
        },
        modifier = modifier.onGloballyPositioned { width = it.size.width.toFloat() },
    ) {
        content()
    }
}
