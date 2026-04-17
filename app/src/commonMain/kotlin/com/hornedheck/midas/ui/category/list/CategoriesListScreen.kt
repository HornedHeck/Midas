package com.hornedheck.midas.ui.category.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Menu
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
import com.hornedheck.midas.ui.components.ColorDot
import com.hornedheck.midas.ui.components.SwipeToDeleteBox
import com.hornedheck.midas.ui.navigation.BottomNavBar
import midas.app.generated.resources.Res
import midas.app.generated.resources.cd_add_category
import midas.app.generated.resources.empty_categories
import midas.app.generated.resources.error_loading_categories
import midas.app.generated.resources.screen_categories
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesListScreen(
    onAddCategory: () -> Unit = {},
    onItemClick: (id: Long) -> Unit = {},
    onItemDelete: (id: Long, name: String) -> Unit = { _, _ -> },
    viewModel: CategoriesListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CategoriesListScreen(
        state = state,
        onAddCategory = onAddCategory,
        onItemClick = onItemClick,
        onItemDelete = onItemDelete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesListScreen(
    state: CategoriesListState,
    onAddCategory: () -> Unit = {},
    onItemClick: (id: Long) -> Unit = {},
    onItemDelete: (id: Long, name: String) -> Unit = { _, _ -> },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screen_categories)) },
            )
        },
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCategory) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.cd_add_category),
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
                is CategoriesListState.Loading -> CategoriesListLoading()
                is CategoriesListState.Empty -> CategoriesListEmpty()
                is CategoriesListState.Content -> CategoriesListContent(
                    items = state.items,
                    onItemClick = onItemClick,
                    onItemDelete = onItemDelete,
                )
                is CategoriesListState.Error -> CategoriesListError(state.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoriesListLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularWavyProgressIndicator()
    }
}

@Composable
private fun CategoriesListEmpty() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = AppDimens.spacing2x),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = stringResource(Res.string.empty_categories),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CategoriesListError(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message.ifEmpty { stringResource(Res.string.error_loading_categories) },
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun CategoriesListContent(
    items: List<CategoryUiItem>,
    onItemClick: (Long) -> Unit,
    onItemDelete: (id: Long, name: String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            SwipeToDeleteBox(onDelete = { onItemDelete(item.id, item.name) }) {
                CategoryItem(item = item, onClick = { onItemClick(item.id) })
            }
            if (index != items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = AppDimens.spacing4x),
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(item: CategoryUiItem, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            ColorDot(
                color = item.color,
                modifier = Modifier.size(AppDimens.spacing5x),
            )
        },
        headlineContent = {
            Text(item.name)
        },
    )
}
