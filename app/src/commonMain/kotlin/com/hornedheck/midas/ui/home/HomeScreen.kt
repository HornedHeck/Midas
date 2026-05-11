package com.hornedheck.midas.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hornedheck.midas.domain.model.dashboard.CategorySpendingSummary
import com.hornedheck.midas.theme.AppDimens
import com.hornedheck.midas.theme.MidasColor
import com.hornedheck.midas.ui.components.ColorDot
import com.hornedheck.midas.ui.components.FullScreenErrorText
import com.hornedheck.midas.ui.components.FullScreenLoading
import com.hornedheck.midas.ui.navigation.BottomNavBar
import com.hornedheck.midas.util.formatAbsAmount
import com.hornedheck.midas.util.formatAmount
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.toDegrees
import kotlinx.datetime.LocalDate
import midas.app.generated.resources.Res
import midas.app.generated.resources.cd_add_transaction
import midas.app.generated.resources.hint_uncategorized
import midas.app.generated.resources.home_category_others
import midas.app.generated.resources.home_category_percentage
import midas.app.generated.resources.home_currency_label
import midas.app.generated.resources.home_delta_percentage
import midas.app.generated.resources.home_empty_message
import midas.app.generated.resources.home_error
import midas.app.generated.resources.home_label_balance
import midas.app.generated.resources.home_label_expenses
import midas.app.generated.resources.home_label_income
import midas.app.generated.resources.home_range_prefix
import midas.app.generated.resources.home_statistics_title
import midas.app.generated.resources.home_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val DonutHoleSize = 0.6f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onCategoryClick: (categoryId: Long?, categoryName: String?, dateFrom: LocalDate, dateTo: LocalDate) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var fabHeightPx by remember { mutableFloatStateOf(0f) }
    val fabSafeBottomSpacing = with(LocalDensity.current) {
        fabHeightPx.toDp()
    } + AppDimens.spacing8x

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.home_title)) })
        },
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                modifier = Modifier.onGloballyPositioned { fabHeightPx = it.size.height.toFloat() },
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.cd_add_transaction)
                )
            }
        },
    ) { paddingValues ->
        when (val currentState = state) {
            HomeUiState.Loading -> FullScreenLoading(Modifier.padding(paddingValues))

            is HomeUiState.Error -> {
                Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    RangeSelector(
                        selectedRange = currentState.selectedRange,
                        onRangeSelected = viewModel::selectRange,
                    )
                    FullScreenErrorText(
                        message = stringResource(Res.string.home_error),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            is HomeUiState.Empty -> {
                HomeEmptyState(
                    state = currentState,
                    onRangeSelected = viewModel::selectRange,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            is HomeUiState.Content -> {
                HomeContent(
                    state = currentState,
                    onRangeSelected = viewModel::selectRange,
                    onCategoryClick = onCategoryClick,
                    fabSafeBottomSpacing = fabSafeBottomSpacing,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun HomeEmptyState(
    state: HomeUiState.Empty,
    onRangeSelected: (HomeRange) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RangeSelector(
            selectedRange = state.selectedRange,
            onRangeSelected = onRangeSelected,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.home_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Content,
    onRangeSelected: (HomeRange) -> Unit,
    onCategoryClick: (categoryId: Long?, categoryName: String?, dateFrom: LocalDate, dateTo: LocalDate) -> Unit,
    fabSafeBottomSpacing: Dp,
    modifier: Modifier,
) {
    val (dateFrom, dateTo) = remember(state.selectedRange) { state.selectedRange.dateRange() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            RangeSelector(
                selectedRange = state.selectedRange,
                onRangeSelected = onRangeSelected,
            )
        }
        item { Spacer(Modifier.height(AppDimens.spacing4x)) }
        item {
            SummaryCard(
                incomeCents = state.incomeCents,
                expensesCents = state.expensesCents,
                netBalanceCents = state.netBalanceCents,
                incomeDelta = state.incomeDeltaPct,
                isIncomeTrendPositive = state.isIncomeTrendPositive,
                expensesDelta = state.expensesDeltaPct,
                isExpensesTrendPositive = state.isExpensesTrendPositive,
                netBalanceDelta = state.netBalanceDeltaPct,
                isNetBalanceTrendPositive = state.isNetBalanceTrendPositive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.spacing4x),
            )
        }
        if (state.categories.isNotEmpty()) {
            item { Spacer(Modifier.height(AppDimens.spacing2x)) }
            item {
                DonutChart(
                    categories = state.categories,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item { Spacer(Modifier.height(AppDimens.spacing2x)) }
            itemsIndexed(state.categories) { index, category ->
                CategoryRow(
                    category = category,
                    onClick = if (!category.isOthers) {
                        { onCategoryClick(category.categoryId, category.name, dateFrom, dateTo) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppDimens.spacing4x),
                )
                if (index < state.categories.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = AppDimens.spacing4x))
                }
            }
        }
        item(key = "fab_safe_spacing") {
            Spacer(modifier = Modifier.height(fabSafeBottomSpacing))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeSelector(
    selectedRange: HomeRange,
    onRangeSelected: (HomeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val prefix = stringResource(Res.string.home_range_prefix)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .padding(horizontal = AppDimens.spacing4x),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$prefix ${selectedRange.text()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            HomeRange.entries.forEach { range ->
                DropdownMenuItem(
                    text = { Text(range.text()) },
                    onClick = {
                        onRangeSelected(range)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun HomeRange.text(): String = stringResource(label)

@Composable
private fun SummaryCard(
    incomeCents: Long,
    expensesCents: Long,
    netBalanceCents: Long,
    incomeDelta: Float?,
    isIncomeTrendPositive: Boolean?,
    expensesDelta: Float?,
    isExpensesTrendPositive: Boolean?,
    netBalanceDelta: Float?,
    isNetBalanceTrendPositive: Boolean?,
    modifier: Modifier,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(AppDimens.spacing3x)) {
            Text(
                text = stringResource(Res.string.home_statistics_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(AppDimens.spacing3x))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryColumn(
                    label = stringResource(Res.string.home_label_income),
                    amountCents = incomeCents,
                    deltaPct = incomeDelta,
                    isTrendPositive = isIncomeTrendPositive,
                    modifier = Modifier.weight(1f),
                )
                SummaryColumn(
                    label = stringResource(Res.string.home_label_expenses),
                    amountCents = expensesCents,
                    deltaPct = expensesDelta,
                    isTrendPositive = isExpensesTrendPositive,
                    modifier = Modifier.weight(1f),
                )
                SummaryColumn(
                    label = stringResource(Res.string.home_label_balance),
                    amountCents = netBalanceCents,
                    deltaPct = netBalanceDelta,
                    isTrendPositive = isNetBalanceTrendPositive,
                    showSign = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    amountCents: Long,
    deltaPct: Float?,
    isTrendPositive: Boolean?,
    modifier: Modifier = Modifier,
    showSign: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (showSign) formatAmount(amountCents) else formatAbsAmount(amountCents),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        DeltaRow(deltaPct = deltaPct, isTrendPositive = isTrendPositive)
    }
}

@Composable
private fun DeltaRow(deltaPct: Float?, isTrendPositive: Boolean?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.home_currency_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (deltaPct != null && isTrendPositive != null) {
            val color = if (isTrendPositive) MidasColor.Income else MidasColor.Expense
            val sign = if (deltaPct >= 0f) "+" else "-"
            Text(
                text = stringResource(
                    Res.string.home_delta_percentage,
                    "$sign${kotlin.math.abs(deltaPct).toInt()}"
                ),
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
        }
    }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun DonutChart(
    categories: List<CategorySpendingSummary>,
    modifier: Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        PieChart(
            values = categories.map { it.percentage },
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            holeSize = DonutHoleSize,
            label = {},
            labelConnector = {},
            slice = { index ->
                val category = categories[index]
                val color = category.color?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceVariant
                val gapDeg = when {
                    categories.size == 1 -> 0f
                    pieSliceData.angle.toDegrees().value.toFloat() > 3.6f -> 1f
                    else -> 0.2f
                }
                DefaultSlice(
                    color = color,
                    antiAlias = true,
                    gap = gapDeg
                )
            },
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategorySpendingSummary,
    onClick: (() -> Unit)?,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = AppDimens.spacing3x),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ColorDot(
            color = category.color,
            modifier = Modifier.size(AppDimens.spacing5x),
        )
        Spacer(Modifier.width(AppDimens.spacing3x))
        Text(
            text = when {
                category.isOthers -> stringResource(Res.string.home_category_others)
                else -> category.name ?: stringResource(Res.string.hint_uncategorized)
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(
                Res.string.home_category_percentage,
                "%.1f".format(category.percentage)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onClick != null) {
            Spacer(Modifier.width(AppDimens.spacing2x))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Spacer(Modifier.width(AppDimens.spacing8x))
        }
    }
}
