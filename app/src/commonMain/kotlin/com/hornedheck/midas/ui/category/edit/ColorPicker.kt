package com.hornedheck.midas.ui.category.edit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hornedheck.midas.theme.MidasAppTheme

private object ColorPickerDefaults {

    val RippleSize = 42.dp
    val Size = 32.dp
    val FillSize = 24.dp
    val StrokeWidth = 2.dp
}

@OptIn(ExperimentalGridApi::class)
@Composable
internal fun ColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    colors: List<Int>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
    ) {
        colors.forEach { color ->
            ColorCircle(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) },
            )
        }
    }
}

@Composable
private fun ColorCircle(
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Canvas(
        modifier = Modifier.selectable(
            selected = isSelected,
            onClick = onClick,
            role = Role.RadioButton,
            interactionSource = null,
            indication = ripple(bounded = false, radius = ColorPickerDefaults.RippleSize / 2),
        ).minimumInteractiveComponentSize().requiredSize(ColorPickerDefaults.Size),
        contentDescription = "",
    ) {
        val stroke = ColorPickerDefaults.StrokeWidth.toPx()

        drawCircle(
            Color(color),
            radius = ColorPickerDefaults.FillSize.toPx() / 2,
            style = Fill,
        )

        if (isSelected) {
            drawCircle(
                Color(color),
                radius = ColorPickerDefaults.Size.toPx() / 2 - stroke / 2,
                style = Stroke(stroke),
            )
        }
    }
}

@Preview
@Composable
private fun ColorPickerPreview() {
    MidasAppTheme {
        ColorPicker(
            selectedColor = CATEGORY_PALETTE.first(),
            onColorSelected = {},
            colors = CATEGORY_PALETTE,
        )
    }
}
