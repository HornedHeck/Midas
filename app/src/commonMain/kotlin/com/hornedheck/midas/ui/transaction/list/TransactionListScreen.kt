package com.hornedheck.midas.ui.transaction.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.theme.MidasColor
import com.hornedheck.midas.ui.components.ColorDot
import com.hornedheck.midas.ui.components.FullScreenEmptyText
import com.hornedheck.midas.ui.components.FullScreenErrorText
import com.hornedheck.midas.ui.components.FullScreenLoading
import com.hornedheck.midas.ui.components.SwipeToDeleteBox
import com.hornedheck.midas.ui.navigation.BottomNavBar
import com.hornedheck.midas.util.format
import com.hornedheck.midas.util.formatAbsAmount
import com.hornedheck.midas.util.formatLong
import midas.app.generated.resources.Res
import midas.app.generated.resources.cd_add_transaction
import midas.app.generated.resources.cd_clear_chip
import midas.app.generated.resources.cd_filter
import midas.app.generated.resources.empty_transactions
import midas.app.generated.resources.empty_transactions_filtered
import midas.app.generated.resources.error_loading_transactions
import midas.app.generated.resources.filter_chip_amount_at_least
import midas.app.generated.resources.filter_chip_amount_at_most
import midas.app.generated.resources.filter_chip_date_after
import midas.app.generated.resources.filter_chip_date_before
import midas.app.generated.resources.hint_none
import midas.app.generated.resources.hint_uncategorized
import midas.app.generated.resources.screen_transactions
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionListScreen(
    onAddTransaction: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onTransactionClick: (Long) -> Unit = {},
    onTransactionDelete: (id: Long, description: String) -> Unit = { _, _ -> },
    viewModel: TransactionListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TransactionListScreen(
        state = state,
        onAddTransaction = onAddTransaction,
        onFilterClick = onFilterClick,
        onTransactionClick = onTransactionClick,
        onTransactionDelete = onTransactionDelete,
        onDismissChip = viewModel::dismissChip,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    state: TransactionListState,
    onAddTransaction: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onTransactionClick: (Long) -> Unit = {},
    onTransactionDelete: (id: Long, description: String) -> Unit = { _, _ -> },
    onDismissChip: (FilterChipKey) -> Unit = {},
) {
    var fabHeightPx by remember { mutableFloatStateOf(0f) }
    val fabSafeBottomSpacing = fabHeightPx.dp + AppDimens.spacing8x

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screen_transactions)) },
                actions = {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = stringResource(Res.string.cd_filter),
                        )
                    }
                },
            )
        },
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    fabHeightPx = coordinates.size.height.toFloat()
                },
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.cd_add_transaction),
                )
            }
        },
    ) { paddingValues ->
        TransactionListBody(
            state = state,
            onTransactionClick = onTransactionClick,
            onTransactionDelete = onTransactionDelete,
            onDismissChip = onDismissChip,
            bottomSpacer = fabSafeBottomSpacing,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

@Composable
private fun TransactionListBody(
    modifier: Modifier,
    state: TransactionListState,
    onTransactionClick: (Long) -> Unit,
    onTransactionDelete: (id: Long, description: String) -> Unit,
    onDismissChip: (FilterChipKey) -> Unit,
    bottomSpacer: Dp,
) {
    Box(modifier = modifier) {
        when (state) {
            is TransactionListState.Loading -> FullScreenLoading(modifier = Modifier.fillMaxSize())
            is TransactionListState.Empty -> EmptyTransactionsContent(
                state = state,
                modifier = Modifier.fillMaxSize(),
            )
            is TransactionListState.Content -> TransactionListContent(
                state = state,
                onTransactionClick = onTransactionClick,
                onTransactionDelete = onTransactionDelete,
                onDismissChip = onDismissChip,
                bottomSpacer = bottomSpacer,
                modifier = Modifier.fillMaxSize(),
            )
            is TransactionListState.Error -> ErrorTransactionsContent(
                state = state,
                onDismissChip = onDismissChip,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun TransactionListContent(
    modifier: Modifier,
    state: TransactionListState.Content,
    onTransactionClick: (Long) -> Unit,
    onTransactionDelete: (id: Long, description: String) -> Unit,
    onDismissChip: (FilterChipKey) -> Unit,
    bottomSpacer: Dp,
) {
    Column(modifier = modifier) {
        if (state.activeChips.isNotEmpty()) {
            ActiveFiltersRow(
                chips = state.activeChips,
                onDismissChip = onDismissChip,
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            state.groups.forEachIndexed { groupIndex, group ->
                stickyHeader(key = group.date) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = group.date.formatLong(),
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
                        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))
                    }
                }
                itemsIndexed(group.transactions, key = { _, item -> item.id }) { index, item ->
                    SwipeToDeleteBox(onDelete = {
                        onTransactionDelete(
                            item.id,
                            item.description
                        )
                    }) {
                        TransactionItem(item = item, onClick = { onTransactionClick(item.id) })
                    }
                    if (groupIndex != state.groups.lastIndex || index != group.transactions.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))
                    }
                }
            }
            item(key = "fab_safe_spacing") {
                Spacer(modifier = Modifier.height(bottomSpacer))
            }
        }
    }
}

@Composable
private fun EmptyTransactionsContent(
    modifier: Modifier,
    state: TransactionListState.Empty,
) {
    FullScreenEmptyText(
        text = if (state.isFiltered) stringResource(Res.string.empty_transactions_filtered)
        else stringResource(Res.string.empty_transactions),
        modifier = modifier,
    )
}

@Composable
private fun ErrorTransactionsContent(
    modifier: Modifier,
    state: TransactionListState.Error,
    onDismissChip: (FilterChipKey) -> Unit,
) {
    Column(modifier = modifier) {
        if (state.activeChips.isNotEmpty()) {
            ActiveFiltersRow(
                chips = state.activeChips,
                onDismissChip = onDismissChip,
            )
        }
        FullScreenErrorText(
            message = state.message.ifEmpty { stringResource(Res.string.error_loading_transactions) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveFiltersRow(
    modifier: Modifier = Modifier,
    chips: List<FilterChipKey>,
    onDismissChip: (FilterChipKey) -> Unit,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.spacing4x, vertical = AppDimens.spacing1x),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppDimens.spacing2x),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppDimens.spacing2x),
    ) {
        chips.forEach { chip ->
            FilterChip(
                selected = true,
                onClick = { onDismissChip(chip) },
                label = { Text(chip.label()) },
                leadingIcon = {
                    if (chip is FilterChipKey.Category) {
                        ColorDot(
                            color = chip.color,
                            modifier = Modifier.size(AppDimens.spacing3x),
                        )
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.cd_clear_chip),
                        modifier = Modifier.size(AppDimens.spacing4x),
                    )
                },
            )
        }
    }
}

@Composable
private fun FilterChipKey.label(): String = when (this) {
    is FilterChipKey.Type -> stringResource(type.label)
    is FilterChipKey.DateFrom -> stringResource(Res.string.filter_chip_date_after, date.format())
    is FilterChipKey.DateTo -> stringResource(Res.string.filter_chip_date_before, date.format())
    is FilterChipKey.AmountFrom -> stringResource(
        Res.string.filter_chip_amount_at_least,
        formatAbsAmount(cents, withCurrency = true)
    )
    is FilterChipKey.AmountTo -> stringResource(
        Res.string.filter_chip_amount_at_most,
        formatAbsAmount(cents, withCurrency = true)
    )
    is FilterChipKey.Category -> name ?: stringResource(Res.string.hint_uncategorized)
}

@Composable
private fun TransactionItem(item: TransactionUiItem, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(item.description, style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            Text(
                item.categoryName ?: stringResource(Res.string.hint_none),
                style = MaterialTheme.typography.bodySmall,
            )
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
        },
    )
}
