package com.hornedheck.midas.ui.transaction.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.theme.MidasColor
import com.hornedheck.midas.util.NoOpInteractionSource
import com.hornedheck.midas.util.formatAmountDetail
import com.hornedheck.midas.util.formatDate
import midas.app.generated.resources.Res
import midas.app.generated.resources.cd_back
import midas.app.generated.resources.cd_delete
import midas.app.generated.resources.cd_edit
import midas.app.generated.resources.error_loading_transaction
import midas.app.generated.resources.hint_none
import midas.app.generated.resources.label_category
import midas.app.generated.resources.label_date
import midas.app.generated.resources.label_description
import midas.app.generated.resources.label_notes
import midas.app.generated.resources.screen_transaction_details
import midas.app.generated.resources.type_expense
import midas.app.generated.resources.type_income
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: (description: String) -> Unit = {},
    viewModel: TransactionDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(transactionId) { viewModel.init(transactionId) }

    TransactionDetailScreen(
        state = state,
        onBack = onBack,
        onEdit = onEdit,
        onDelete = onDelete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    state: TransactionDetailState,
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: (description: String) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screen_transaction_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
                        )
                    }
                },
                actions = {
                    if (state is TransactionDetailState.Content) {
                        IconButton(onClick = { onDelete(state.description) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.cd_delete),
                            )
                        }
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(Res.string.cd_edit),
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (state) {
                is TransactionDetailState.Loading -> TransactionDetailLoading()
                is TransactionDetailState.Error -> TransactionDetailError()
                is TransactionDetailState.Content -> TransactionDetailContent(state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TransactionDetailLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularWavyProgressIndicator()
    }
}

@Composable
private fun TransactionDetailError() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.error_loading_transaction),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TransactionDetailContent(state: TransactionDetailState.Content) {
    Column(modifier = Modifier.fillMaxSize()) {
        AmountHeader(
            amountCents = state.amountCents,
            isExpense = state.isExpense,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppDimens.spacing4x),
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))

        ListItem(
            overlineContent = { Text(stringResource(Res.string.label_description)) },
            headlineContent = { Text(state.description) },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))

        ListItem(
            overlineContent = { Text(stringResource(Res.string.label_date)) },
            headlineContent = { Text(formatDate(state.date)) },
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))

        ListItem(
            overlineContent = { Text(stringResource(Res.string.label_category)) },
            headlineContent = {
                Text(state.categoryName ?: stringResource(Res.string.hint_none))
            },
        )

        if (!state.notes.isNullOrBlank()) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))
            ListItem(
                overlineContent = { Text(stringResource(Res.string.label_notes)) },
                headlineContent = { Text(state.notes) },
            )
        }
    }
}

@Composable
private fun AmountHeader(
    amountCents: Long,
    isExpense: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing3x),
    ) {
        Text(
            text = formatAmountDetail(amountCents),
            style = MaterialTheme.typography.displayMedium,
            color = if (isExpense) MidasColor.Expense else MidasColor.Income,
        )

        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = true,
                onClick = {},
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 1),
                icon = {},
                interactionSource = NoOpInteractionSource,
            ) {
                Text(
                    if (isExpense) {
                        stringResource(Res.string.type_expense)
                    } else {
                        stringResource(Res.string.type_income)
                    }
                )
            }
        }
    }
}
