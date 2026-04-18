package com.hornedheck.midas.ui.transaction.delete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.ui.components.DeleteConfirmationDialog
import midas.app.generated.resources.Res
import midas.app.generated.resources.dialog_delete_transaction_body
import midas.app.generated.resources.dialog_delete_transaction_title
import midas.app.generated.resources.error_delete_transaction_failed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeleteTransactionScreen(
    transactionId: Long,
    description: String,
    onDismiss: () -> Unit = {},
    onDeleted: () -> Unit = {},
    viewModel: DeleteTransactionViewModel = koinViewModel(parameters = { parametersOf(transactionId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isSuccess = state is DeleteTransactionState.Success
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onDeleted()
            viewModel.clearSuccess()
        }
    }

    DeleteTransactionScreen(
        state = state,
        description = description,
        onDismiss = onDismiss,
        onConfirmDelete = viewModel::confirmDelete,
    )
}

@Composable
fun DeleteTransactionScreen(
    state: DeleteTransactionState,
    description: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    DeleteConfirmationDialog(
        title = stringResource(Res.string.dialog_delete_transaction_title),
        body = stringResource(Res.string.dialog_delete_transaction_body, description),
        errorMessage = if (state is DeleteTransactionState.Error) {
            stringResource(Res.string.error_delete_transaction_failed)
        } else null,
        isLoading = state is DeleteTransactionState.Loading,
        onDismiss = onDismiss,
        onConfirm = onConfirmDelete,
    )
}

