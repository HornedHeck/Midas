package com.hornedheck.midas.ui.rules.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.ui.components.ColorDot
import com.hornedheck.midas.ui.components.FullScreenEmptyText
import com.hornedheck.midas.ui.components.FullScreenErrorText
import com.hornedheck.midas.ui.components.FullScreenLoading
import com.hornedheck.midas.ui.components.SwipeToDeleteBox
import midas.app.generated.resources.Res
import midas.app.generated.resources.cd_add_rule
import midas.app.generated.resources.cd_back
import midas.app.generated.resources.cd_move_down
import midas.app.generated.resources.cd_move_up
import midas.app.generated.resources.cd_reapply_rules
import midas.app.generated.resources.empty_rules
import midas.app.generated.resources.error_loading_rules
import midas.app.generated.resources.error_reapply_failed
import midas.app.generated.resources.screen_rules
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RulesListScreen(
    onBack: () -> Unit = {},
    onAddRule: () -> Unit = {},
    onRuleClick: (id: Long) -> Unit = {},
    viewModel: RulesListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val reapplyStatus = (state as? RulesListState.Content)?.reapplyStatus
    LaunchedEffect(reapplyStatus) {
        when (reapplyStatus) {
            is ReapplyStatus.Error -> {
                snackbarHostState.showSnackbar(getString(Res.string.error_reapply_failed))
                viewModel.clearReapplyStatus()
            }
            else -> Unit
        }
    }

    RulesListScreen(
        state = state,
        dragItems = viewModel.dragItems,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onAddRule = onAddRule,
        onRuleClick = onRuleClick,
        onRuleDelete = viewModel::deleteRule,
        onReapply = viewModel::reapply,
        onMoveItem = viewModel::moveItem,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesListScreen(
    state: RulesListState,
    dragItems: List<RuleUiItem>,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit = {},
    onAddRule: () -> Unit = {},
    onRuleClick: (id: Long) -> Unit = {},
    onRuleDelete: (id: Long) -> Unit = {},
    onReapply: () -> Unit = {},
    onMoveItem: (from: Int, to: Int) -> Unit = { _, _ -> },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screen_rules)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
                        )
                    }
                },
                actions = {
                    val isReapplying =
                        ((state as? RulesListState.Content)?.reapplyStatus) is ReapplyStatus.Loading
                    IconButton(onClick = onReapply, enabled = !isReapplying) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(Res.string.cd_reapply_rules),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRule) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.cd_add_rule))
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (state) {
                is RulesListState.Loading -> FullScreenLoading()
                is RulesListState.Empty -> FullScreenEmptyText(stringResource(Res.string.empty_rules))
                is RulesListState.Error -> FullScreenErrorText(
                    state.message.ifEmpty { stringResource(Res.string.error_loading_rules) }
                )
                is RulesListState.Content -> RulesListContent(
                    items = dragItems,
                    onRuleClick = onRuleClick,
                    onRuleDelete = onRuleDelete,
                    onMoveItem = onMoveItem,
                )
            }
        }
    }
}

@Composable
private fun RulesListContent(
    items: List<RuleUiItem>,
    onRuleClick: (Long) -> Unit,
    onRuleDelete: (Long) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            SwipeToDeleteBox(onDelete = { onRuleDelete(item.id) }) {
                RuleItem(
                    item = item,
                    isFirst = index == 0,
                    isLast = index == items.lastIndex,
                    onMoveUp = { onMoveItem(index, index - 1) },
                    onMoveDown = { onMoveItem(index, index + 1) },
                    onClick = { onRuleClick(item.id) },
                )
            }
            if (index != items.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))
            }
        }
    }
}

@Composable
private fun RuleItem(
    item: RuleUiItem,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            ColorDot(
                color = item.categoryColor ?: MaterialTheme.colorScheme.surfaceVariant.hashCode(),
                modifier = Modifier.size(AppDimens.spacing5x),
            )
        },
        headlineContent = { Text(item.label) },
        supportingContent = item.categoryName?.let { name -> { Text(name) } },
        trailingContent = {
            Row {
                IconButton(onClick = onMoveUp, enabled = !isFirst) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(Res.string.cd_move_up),
                    )
                }
                IconButton(onClick = onMoveDown, enabled = !isLast) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(Res.string.cd_move_down),
                    )
                }
            }
        },
    )
}
