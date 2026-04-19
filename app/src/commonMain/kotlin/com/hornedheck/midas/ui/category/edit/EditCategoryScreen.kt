package com.hornedheck.midas.ui.category.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.theme.MidasAppTheme
import midas.app.generated.resources.Res
import midas.app.generated.resources.action_cancel
import midas.app.generated.resources.action_save
import midas.app.generated.resources.label_color
import midas.app.generated.resources.label_name
import midas.app.generated.resources.screen_create_category
import midas.app.generated.resources.screen_edit_category
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EditCategoryScreen(
    categoryId: Long?,
    onDismiss: () -> Unit = {},
    onSaved: () -> Unit = {},
    viewModel: EditCategoryViewModel = koinViewModel(parameters = { parametersOf(categoryId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.status) {
        if (state.status is EditCategoryStatus.Success) {
            onSaved()
            viewModel.clearSuccess()
        }
    }

    EditCategoryScreen(
        state = state,
        isEditMode = categoryId != null,
        onDismiss = onDismiss,
        onSave = viewModel::save,
        onColorSelected = viewModel::selectColor,
    )
}

@Composable
fun EditCategoryScreen(
    state: EditCategoryState,
    isEditMode: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onColorSelected: (Int) -> Unit,
) {
    val isLoading = state.status is EditCategoryStatus.Loading

    Surface(
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.spacing6x),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing5x),
        ) {
            Text(
                text = stringResource(
                    if (isEditMode) Res.string.screen_edit_category else Res.string.screen_create_category
                ),
                style = MaterialTheme.typography.headlineSmall,
            )

            OutlinedTextField(
                state = state.form.nameState,
                label = { Text(stringResource(Res.string.label_name)) },
                isError = state.form.nameError != null,
                supportingText = state.form.nameError?.let { res -> { Text(stringResource(res)) } },
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.SingleLine,
                enabled = !isLoading,
            )

            Column {
                Text(
                    modifier = Modifier.padding(start = AppDimens.spacing4x),
                    text = stringResource(Res.string.label_color),
                    style = MaterialTheme.typography.labelMedium,
                )

                ColorPicker(
                    selectedColor = state.form.selectedColor,
                    onColorSelected = onColorSelected,
                    colors = CATEGORY_PALETTE,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (state.status is EditCategoryStatus.Error) {
                Text(
                    text = stringResource(state.status.message),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.action_cancel))
                }
                TextButton(onClick = onSave, enabled = !isLoading) {
                    Text(stringResource(Res.string.action_save))
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCategoryEdit() {
    MidasAppTheme {
        EditCategoryScreen(
            state = EditCategoryState(),
            isEditMode = false,
            onDismiss = {},
            onSave = {},
            onColorSelected = {},
        )
    }
}
