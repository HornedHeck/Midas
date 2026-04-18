package com.hornedheck.midas.ui.category.delete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.ui.components.DeleteConfirmationDialog
import midas.app.generated.resources.Res
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
    DeleteConfirmationDialog(
        title = stringResource(Res.string.dialog_delete_category_title),
        body = stringResource(Res.string.dialog_delete_category_body, name),
        errorMessage = if (state is DeleteCategoryState.Error) {
            stringResource(Res.string.error_delete_category_failed)
        } else null,
        isLoading = state is DeleteCategoryState.Loading,
        onDismiss = onDismiss,
        onConfirm = onConfirmDelete,
    )
}

