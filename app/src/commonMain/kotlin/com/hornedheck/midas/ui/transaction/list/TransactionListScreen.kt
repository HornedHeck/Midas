package com.hornedheck.midas.ui.transaction.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.theme.MidasColor
import com.hornedheck.midas.ui.components.ColorDot
import com.hornedheck.midas.ui.components.SwipeToDeleteBox
import com.hornedheck.midas.ui.navigation.BottomNavBar
import com.hornedheck.midas.util.formatDate
import midas.app.generated.resources.Res
import midas.app.generated.resources.cd_add_transaction
import midas.app.generated.resources.empty_transactions
import midas.app.generated.resources.error_loading_transactions
import midas.app.generated.resources.hint_none
import midas.app.generated.resources.screen_transactions
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionListScreen(
    onAddTransaction: () -> Unit = {},
    onTransactionClick: (Long) -> Unit = {},
    onTransactionDelete: (id: Long, description: String) -> Unit = { _, _ -> },
    viewModel: TransactionListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TransactionListScreen(
        state = state,
        onAddTransaction = onAddTransaction,
        onTransactionClick = onTransactionClick,
        onTransactionDelete = onTransactionDelete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    state: TransactionListState,
    onAddTransaction: () -> Unit = {},
    onTransactionClick: (Long) -> Unit = {},
    onTransactionDelete: (id: Long, description: String) -> Unit = { _, _ -> },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screen_transactions)) },
            )
        },
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.cd_add_transaction)
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (state) {
                is TransactionListState.Loading -> TransactionListLoading()
                is TransactionListState.Empty -> TransactionListEmpty()
                is TransactionListState.Content -> TransactionListContent(
                    state.groups,
                    onTransactionClick,
                    onTransactionDelete
                )

                is TransactionListState.Error -> TransactionListError(state.message)
            }
        }
    }
}

@Composable
private fun TransactionListEmpty() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = AppDimens.spacing2x),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = stringResource(Res.string.empty_transactions),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TransactionListLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularWavyProgressIndicator()
    }
}

@Composable
private fun TransactionListContent(
    groups: List<TransactionGroup>,
    onTransactionClick: (Long) -> Unit,
    onTransactionDelete: (id: Long, description: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        groups.forEachIndexed { groupIndex, group ->
            stickyHeader(key = group.date) {
                Text(
                    text = formatDate(group.date),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = AppDimens.spacing4x,
                            end = AppDimens.spacing4x,
                            top = AppDimens.spacing2x,
                            bottom = AppDimens.spacing1x,
                        ),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = AppDimens.spacing4x
                    )
                )
            }
            itemsIndexed(group.transactions, key = { _, item -> item.id }) { index, item ->
                SwipeToDeleteBox(onDelete = { onTransactionDelete(item.id, item.description) }) {
                    TransactionItem(item = item, onClick = { onTransactionClick(item.id) })
                }
                if (groupIndex != groups.lastIndex || index != group.transactions.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            horizontal = AppDimens.spacing4x
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionListError(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message.ifEmpty { stringResource(Res.string.error_loading_transactions) },
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun TransactionItem(item: TransactionUiItem, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(item.description, style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Text(item.categoryName ?: stringResource(Res.string.hint_none), style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            Text(
                text = item.formattedAmount,
                color = if (item.isExpense) MidasColor.Expense else MidasColor.Income,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        leadingContent = {
            ColorDot(
                color = item.categoryColor,
                modifier = Modifier.size(AppDimens.spacing5x),
            )
        }
    )
}
