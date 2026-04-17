package com.hornedheck.midas.ui.category.delete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.theme.AppDimens
import midas.app.generated.resources.Res
import midas.app.generated.resources.action_cancel
import midas.app.generated.resources.action_delete
import midas.app.generated.resources.dialog_delete_category_body
import midas.app.generated.resources.dialog_delete_category_title
import midas.app.generated.resources.error_delete_category_failed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeleteCategoryScreen(
    categoryId: Long,
    name: String,
    onDismiss: () -> Unit = {},
    onDeleted: () -> Unit = {},
    viewModel: DeleteCategoryViewModel = koinViewModel(parameters = { parametersOf(categoryId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isSuccess = state is DeleteCategoryState.Success
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onDeleted()
            viewModel.clearSuccess()
        }
    }

    DeleteCategoryScreen(
        state = state,
        name = name,
        onDismiss = onDismiss,
        onConfirmDelete = viewModel::confirmDelete,
    )
}

@Composable
fun DeleteCategoryScreen(
    state: DeleteCategoryState,
    name: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Surface(
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.spacing6x),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4x),
        ) {
            Text(
                text = stringResource(Res.string.dialog_delete_category_title),
                style = MaterialTheme.typography.headlineSmall,
            )

            if (state is DeleteCategoryState.Error) {
                Text(
                    text = stringResource(Res.string.error_delete_category_failed),
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Text(stringResource(Res.string.dialog_delete_category_body, name))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.action_cancel))
                }
                TextButton(
                    onClick = onConfirmDelete,
                    enabled = state is DeleteCategoryState.Idle || state is DeleteCategoryState.Error,
                ) {
                    Text(
                        text = stringResource(Res.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
