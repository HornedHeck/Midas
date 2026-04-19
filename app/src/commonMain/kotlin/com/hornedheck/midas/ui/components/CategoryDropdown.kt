package com.hornedheck.midas.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hornedheck.midas.domain.model.CategorySource
import com.hornedheck.midas.ui.transaction.add.CategoryOption
import midas.app.generated.resources.Res
import midas.app.generated.resources.hint_none
import midas.app.generated.resources.label_category_auto
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryDropdown(
    categories: List<CategoryOption>,
    selectedId: Long?,
    enabled: Boolean,
    label: StringResource,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    categorySource: CategorySource = CategorySource.MANUAL,
    onAutoSelected: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val autoLabel =
        if (onAutoSelected != null) stringResource(Res.string.label_category_auto) else null
    val selectedName = when {
        autoLabel != null && categorySource != CategorySource.MANUAL -> autoLabel
        else -> categories.find { it.id == selectedId }?.name
            ?: stringResource(Res.string.hint_none)
    }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded && enabled) },
            modifier = Modifier
                .fillMaxWidth()
                .exposedDropdownSize()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            enabled = enabled,
        )
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            if (onAutoSelected != null && autoLabel != null) {
                DropdownMenuItem(
                    text = { Text(autoLabel) },
                    onClick = {
                        onAutoSelected()
                        expanded = false
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.hint_none)) },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                },
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
