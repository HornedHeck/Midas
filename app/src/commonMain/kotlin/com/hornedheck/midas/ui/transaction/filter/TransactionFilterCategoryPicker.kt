package com.hornedheck.midas.ui.transaction.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hornedheck.midas.domain.model.Category
import com.hornedheck.midas.theme.AppDimens
import midas.app.generated.resources.Res
import midas.app.generated.resources.action_cancel
import midas.app.generated.resources.action_ok
import midas.app.generated.resources.hint_uncategorized
import midas.app.generated.resources.label_categories
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TransactionFilterCategoryPickerDialog(
    categories: List<Category>,
    selectedIds: Set<Long?>,
    onConfirm: (Set<Long?>) -> Unit,
    onDismiss: () -> Unit,
) {
    var current by remember(selectedIds) { mutableStateOf(selectedIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.label_categories)) },
        text = {
            Column {
                CategoryCheckboxItem(
                    name = stringResource(Res.string.hint_uncategorized),
                    checked = current.contains(null),
                    onCheckedChange = { checked ->
                        if (checked) {
                            current += null
                        } else {
                            current -= null
                        }
                    },
                )
                categories.forEach { category ->
                    CategoryCheckboxItem(
                        name = category.name,
                        checked = current.contains(category.id),
                        onCheckedChange = { checked ->
                            if (checked) {
                                current += category.id
                            } else {
                                current -= category.id
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(current) }) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

@Composable
private fun CategoryCheckboxItem(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = AppDimens.spacing1x),
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(name, style = MaterialTheme.typography.bodyLarge)
    }
}
